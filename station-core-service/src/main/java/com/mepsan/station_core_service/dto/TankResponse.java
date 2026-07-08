package com.mepsan.station_core_service.dto;
import java.math.BigDecimal;
public record TankResponse(String tankCode, String fuelType, BigDecimal currentPrice) {}
