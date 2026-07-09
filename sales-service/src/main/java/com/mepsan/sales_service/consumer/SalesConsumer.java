package com.mepsan.sales_service.consumer;

import com.mepsan.sales_service.config.RabbitMQConfig;
import com.mepsan.sales_service.dto.SalesMessage;
import com.mepsan.sales_service.service.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SalesConsumer {

    private final SalesService salesService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SALES)
    public void consumeSales(SalesMessage message, @Header(required = false, name = "x-death") List<Map<String, Object>> xDeath) {

        long retryCount = getRetryCount(xDeath);

        // İlk geliş: 0. Wait'ten ilk dönüş: 1. Wait'ten ikinci dönüş: 2. Wait'ten üçüncü dönüş: 3.
        // Eğer retryCount >= 3 ise, bu işlem 3 kez yeniden denenmiş (toplamda 4 kez kuyruğa girmiş) demektir.
        if (retryCount >= 3) {
            log.error("Maksimum yeniden deneme sayısına ulaşıldı (3 retry). TIMEOUT_REJECTED olarak kaydediliyor: {}", message.transactionId());
            salesService.saveTimeoutRejected(message);
            return;
        }

        try {
            salesService.processSales(message);
        } catch (AmqpRejectAndDontRequeueException e) {
            log.warn("Satis kuyruga geri itildi (Retry: {}): {}", retryCount + 1, message.transactionId());
            throw e;
        }
    }

    private long getRetryCount(List<Map<String, Object>> xDeath) {
        if (xDeath != null && !xDeath.isEmpty()) {
            return (Long) xDeath.get(0).get("count");
        }
        return 0;
    }
}
