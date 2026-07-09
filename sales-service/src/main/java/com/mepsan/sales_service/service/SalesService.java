package com.mepsan.sales_service.service;
import com.mepsan.sales_service.entity.Transaction;
import com.mepsan.sales_service.dto.SalesMessage;
import com.mepsan.sales_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesService {

    private final TransactionRepository transactionRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    @Transactional
    public void processSales(SalesMessage msg) {
        if (transactionRepository.existsByTransactionUuid(msg.transactionId())) {
            log.warn("Mükerrer islem reddedildi (Kod seviyesi kontrol): {}", msg.transactionId());
            return;
        }

        Boolean isStationValid = redisTemplate.opsForSet().isMember("valid:stations", msg.stationCode());
        Boolean isPumpValid = redisTemplate.opsForSet().isMember("valid:pumps:" + msg.stationCode(), String.valueOf(msg.pumpNumber()));

        if (Boolean.FALSE.equals(isStationValid) || Boolean.FALSE.equals(isPumpValid)) {
            saveTransaction(msg, "INVALID_REFERENCE");
            return;
        }

        String lockKey = "lock:pump:" + msg.stationCode() + ":" + msg.pumpNumber();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    String priceKey = "price:" + msg.stationCode() + ":" + msg.fuelType();
                    String cachedPrice = redisTemplate.opsForValue().get(priceKey);

                    if (cachedPrice == null) {
                        saveTransaction(msg, "PRICE_NOT_FOUND");
                        return;
                    }

                    BigDecimal expectedPrice = new BigDecimal(cachedPrice);
                    BigDecimal payloadPrice = BigDecimal.valueOf(msg.unitPrice());

                    if (expectedPrice.compareTo(payloadPrice) != 0) {
                        log.warn("Fiyat uyusmazligi. Beklenen: {}, Gelen: {}", expectedPrice, payloadPrice);
                        saveTransaction(msg, "REJECTED_PRICE");
                        return;
                    }

                    saveTransaction(msg, "SUCCESS");
                    log.info("Satis basariyla kaydedildi: {}", msg.transactionId());

                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                throw new AmqpRejectAndDontRequeueException("Pompa mesgul, DLQ'ya yonlendiriliyor");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Kilit beklenirken hata olustu", e);
        }
    }

    @Transactional
    public void saveTimeoutRejected(SalesMessage msg) {
        if (!transactionRepository.existsByTransactionUuid(msg.transactionId())) {
            saveTransaction(msg, "TIMEOUT_REJECTED");
        }
    }

    private void saveTransaction(SalesMessage msg, String status) {
        try {
            Transaction txn = new Transaction();
            txn.setTransactionUuid(msg.transactionId());
            txn.setStationCode(msg.stationCode());
            txn.setPumpNumber(msg.pumpNumber());
            txn.setNozzleNumber(msg.nozzleNumber());
            txn.setFuelType(msg.fuelType());
            txn.setVolume(BigDecimal.valueOf(msg.volume()));
            txn.setUnitPrice(BigDecimal.valueOf(msg.unitPrice()));
            txn.setTotalAmount(BigDecimal.valueOf(msg.totalAmount()));
            txn.setPlateNumber(msg.plateNumber());
            txn.setTransactionTime(Instant.parse(msg.timestamp()));
            txn.setStatus(status);

            transactionRepository.saveAndFlush(txn); // Hatanın anında fırlatılması için flush ediyoruz

        } catch (DataIntegrityViolationException e) {
            // Yarış durumunda DB seviyesinde mükerrer kayıt engellenirse sessizce yut
            log.warn("Veritabani kısıtlaması (Mükerrer UUID) engellendi: {}", msg.transactionId());
        }
    }
}
