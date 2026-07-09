package com.mepsan.station_core_service.exception;

public class StationNotFoundException extends RuntimeException {
    public StationNotFoundException(String stationCode) {
        super(String.format("İstasyon '%s' bulunamadı.", stationCode));
    }
}
