package com.mepsan.iot_simulator_service.scheduler;

import com.mepsan.iot_simulator_service.config.RabbitMQConfig;
import com.mepsan.iot_simulator_service.dto.RefillPayload;
import com.mepsan.iot_simulator_service.dto.SalesPayload;
import com.mepsan.iot_simulator_service.dto.TelemetryPayload;
import com.mepsan.iot_simulator_service.generator.DataGenerator;
import com.mepsan.iot_simulator_service.publisher.RabbitMQPublisher;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimulationScheduler {

    private final DataGenerator generator;
    private final RabbitMQPublisher publisher;

    @Getter @Setter
    private boolean isRunning = false; // Varsayılan olarak duruk başlar, API ile tetiklenir.

    @Scheduled(fixedRateString = "${simulator.sales-rate-ms}")
    public void simulateSales() {
        if (!isRunning) return;
        SalesPayload payload = generator.generateSales();
        String routingKey = "sales.kny." + payload.stationCode();
        publisher.publish(RabbitMQConfig.EXCHANGE_SALES, routingKey, payload);
    }

    @Scheduled(fixedRateString = "${simulator.telemetry-rate-ms}")
    public void simulateTelemetry() {
        if (!isRunning) return;
        TelemetryPayload payload = generator.generateTelemetry();
        String routingKey = "telemetry.kny." + payload.stationCode();
        publisher.publish(RabbitMQConfig.EXCHANGE_TELEMETRY, routingKey, payload);
    }

    @Scheduled(fixedRateString = "${simulator.refill-rate-ms}")
    public void simulateRefill() {
        if (!isRunning) return;
        RefillPayload payload = generator.generateRefill();
        String routingKey = "refill.kny." + payload.stationCode();
        publisher.publish(RabbitMQConfig.EXCHANGE_REFILL, routingKey, payload);
        log.info("TANKER IKMALI GERCEKLESTI: {} -> {} Litre", payload.stationCode(), payload.refillVolume());
    }
}
