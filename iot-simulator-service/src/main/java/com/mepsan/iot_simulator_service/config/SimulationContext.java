package com.mepsan.iot_simulator_service.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class SimulationContext {
    @Value("${simulator.station-count}")
    private int stationCount;

    @Getter
    private final List<String> stationPool = new ArrayList<>();

    // Tank ve Yakıt Tipleri Havuzu
    public record TankInfo(String tankId, String fuelType) {}
    @Getter
    private final List<TankInfo> tankPool = List.of(
            new TankInfo("T-01", "DIESEL"),
            new TankInfo("T-02", "GASOLINE"),
            new TankInfo("T-03", "LPG")
    );

    private final Random random = new Random();

    @PostConstruct
    public void initStations() {
        for (int i = 1; i <= stationCount; i++) {
            stationPool.add(String.format("ST-KNY-%03d", i));
        }
    }

    public String getRandomStation() {
        return stationPool.get(random.nextInt(stationPool.size()));
    }

    public TankInfo getRandomTank() {
        return tankPool.get(random.nextInt(tankPool.size()));
    }
}
