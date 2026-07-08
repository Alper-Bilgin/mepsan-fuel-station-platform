package com.mepsan.iot_simulator_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "simulator")
@Getter
@Setter
public class SimulatorProperties {
    // YAML'daki simulator.prices alanını otomatik olarak bu Map'e bağlar
    private Map<String, Double> prices;
}
