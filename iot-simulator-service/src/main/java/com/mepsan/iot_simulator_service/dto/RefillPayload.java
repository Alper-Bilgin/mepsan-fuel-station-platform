package com.mepsan.iot_simulator_service.dto;

public record RefillPayload(
        String refillId,
        String stationCode,
        String tankId,
        String fuelType,
        double refillVolume,
        String supplierPlate,
        String timestamp
) {}