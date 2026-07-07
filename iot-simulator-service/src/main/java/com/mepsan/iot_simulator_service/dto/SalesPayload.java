package com.mepsan.iot_simulator_service.dto;

public record SalesPayload(String transactionId, String stationCode, int pumpNumber, int nozzleNumber, String fuelType, double volume, double unitPrice, double totalAmount, String plateNumber, String timestamp) {}
