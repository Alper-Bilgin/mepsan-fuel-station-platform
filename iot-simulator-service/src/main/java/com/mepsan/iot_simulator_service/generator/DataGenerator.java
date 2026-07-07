package com.mepsan.iot_simulator_service.generator;

import com.mepsan.iot_simulator_service.config.SimulationContext;
import com.mepsan.iot_simulator_service.dto.RefillPayload;
import com.mepsan.iot_simulator_service.dto.SalesPayload;
import com.mepsan.iot_simulator_service.dto.TelemetryPayload;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class DataGenerator {

    private final SimulationContext context;

    @Value("${simulator.base-price}")
    private double basePrice;

    // Thread-safe Faker ve ID yönetimi
    private final ThreadLocal<Faker> threadLocalFaker = ThreadLocal.withInitial(Faker::new);
    private final AtomicReference<String> lastTransactionId = new AtomicReference<>(UUID.randomUUID().toString());

    public SalesPayload generateSales() {
        Faker faker = threadLocalFaker.get();
        String stationCode = context.getRandomStation();
        SimulationContext.TankInfo tankInfo = context.getRandomTank();
        double volume = faker.number().randomDouble(2, 10, 60);

        boolean isChaos = faker.number().numberBetween(1, 100) <= 5;

        String txnId = UUID.randomUUID().toString();
        double unitPrice = basePrice;

        if (isChaos) {
            if (faker.bool().bool()) {
                unitPrice = 10.00; // Fiyat uyuşmazlığı hatası
            } else {
                txnId = lastTransactionId.get(); // Idempotency hatası (Mükerrer UUID)
            }
        } else {
            lastTransactionId.set(txnId);
        }

        double totalAmount = Math.round((volume * unitPrice) * 100.0) / 100.0;
        String plate = faker.number().numberBetween(10, 81) + faker.letterify("???", true).toUpperCase() + faker.number().numberBetween(100, 9999);

        return new SalesPayload(txnId, stationCode, faker.number().numberBetween(1, 8), 1, tankInfo.fuelType(), volume, unitPrice, totalAmount, plate, Instant.now().toString());
    }

    public TelemetryPayload generateTelemetry() {
        Faker faker = threadLocalFaker.get();
        String stationCode = context.getRandomStation();
        SimulationContext.TankInfo tankInfo = context.getRandomTank();

        double currentVolume = faker.number().randomDouble(2, 5000, 20000);
        double temp = faker.number().randomDouble(2, 15, 25);
        double water = faker.number().randomDouble(2, 0, 5);

        // Chaos Injection: Su seviyesini eşiğin üstüne çıkar
        if (faker.number().numberBetween(1, 100) <= 5) {
            water = faker.number().randomDouble(2, 30, 50);
        }

        return new TelemetryPayload(stationCode, tankInfo.tankId(), tankInfo.fuelType(), currentVolume, temp, water, Instant.now().toString());
    }

    public RefillPayload generateRefill() {
        Faker faker = threadLocalFaker.get();
        String stationCode = context.getRandomStation();
        SimulationContext.TankInfo tankInfo = context.getRandomTank();
        String refillId = UUID.randomUUID().toString();
        String supplierPlate = "34TANKER" + faker.number().numberBetween(100, 999);

        return new RefillPayload(refillId, stationCode, tankInfo.tankId(), tankInfo.fuelType(), 10000.0, supplierPlate, Instant.now().toString());
    }
}
