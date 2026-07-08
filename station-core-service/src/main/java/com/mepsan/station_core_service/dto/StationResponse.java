package com.mepsan.station_core_service.dto;
import java.util.List;
public record StationResponse(String stationCode, String name, String city, String status, List<TankResponse> tanks) {}
