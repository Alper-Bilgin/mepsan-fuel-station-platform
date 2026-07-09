package com.mepsan.station_core_service.consumer;

import com.mepsan.station_core_service.config.RabbitMQConsumerConfig;
import com.mepsan.station_core_service.dto.RefillMessage;
import com.mepsan.station_core_service.entity.Refill;
import com.mepsan.station_core_service.repository.RefillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefillConsumer {

    private final RefillRepository refillRepository;

    @RabbitListener(queues = RabbitMQConsumerConfig.QUEUE_REFILL)
    @Transactional
    public void consumeRefill(RefillMessage message) {
        if (refillRepository.existsByRefillId(message.refillId())) {
            log.warn("Mükerrer refill mesajı görmezden gelindi: {}", message.refillId());
            return;
        }

        Refill refill = new Refill();
        refill.setRefillId(message.refillId());
        refill.setStationCode(message.stationCode());
        refill.setTankCode(message.tankId());
        refill.setFuelType(message.fuelType());
        refill.setRefillVolume(message.refillVolume());
        refill.setSupplierPlate(message.supplierPlate());
        refill.setRefillTime(Instant.parse(message.timestamp()));

        refillRepository.save(refill);
        log.info("Refill başarıyla kaydedildi: {} -> {} L", message.stationCode(), message.refillVolume());
    }
}
