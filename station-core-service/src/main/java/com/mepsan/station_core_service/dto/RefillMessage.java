package com.mepsan.station_core_service.dto;

public record RefillMessage(
        String refillId,
        String stationCode,
        String tankId,
        String fuelType,
        double refillVolume,
        String supplierPlate,
        String timestamp
) {}
