package com.mepsan.telemetry_service.dto;

public record TelemetryMessage(
        String stationCode,
        String tankId,
        String fuelType,
        double currentVolume,
        double temperature,
        double waterLevel,
        String timestamp
) {}
