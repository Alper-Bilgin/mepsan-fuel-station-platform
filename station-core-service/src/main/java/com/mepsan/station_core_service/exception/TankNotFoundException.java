package com.mepsan.station_core_service.exception;

public class TankNotFoundException extends RuntimeException {
    public TankNotFoundException(String stationCode, String tankCode) {
        super(String.format("İstasyon '%s' için tank '%s' bulunamadı.", stationCode, tankCode));
    }
}
