package com.mepsan.telemetry_service.consumer;

import com.mepsan.telemetry_service.config.RabbitMQConfig;
import com.mepsan.telemetry_service.dto.TelemetryMessage;
import com.mepsan.telemetry_service.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetryConsumer {

    private final TelemetryService telemetryService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TELEMETRY)
    public void consumeTelemetry(TelemetryMessage message) {
        telemetryService.processTelemetry(message);
    }
}
