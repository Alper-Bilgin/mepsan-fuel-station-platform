package com.mepsan.iot_simulator_service.dto;

public record TelemetryPayload(String stationCode, String tankId, String fuelType, double currentVolume, double temperature, double waterLevel, String timestamp) {}
