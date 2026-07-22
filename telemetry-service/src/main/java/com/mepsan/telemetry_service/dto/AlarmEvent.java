package com.mepsan.telemetry_service.dto;

public record AlarmEvent(
        String stationCode,
        String tankId,
        String alarmType,
        double actualValue,
        double thresholdValue,
        String timestamp
) {}
