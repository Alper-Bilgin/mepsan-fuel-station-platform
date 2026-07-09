package com.mepsan.sales_service.dto;
public record SalesMessage(String transactionId, String stationCode, int pumpNumber, int nozzleNumber, String fuelType, double volume, double unitPrice, double totalAmount, String plateNumber, String timestamp) {}
