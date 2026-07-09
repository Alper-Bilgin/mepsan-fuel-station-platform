package com.mepsan.station_core_service.controller;

import com.mepsan.station_core_service.dto.PriceUpdateRequest;
import com.mepsan.station_core_service.dto.StationResponse;
import com.mepsan.station_core_service.dto.TankResponse;
import com.mepsan.station_core_service.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public List<StationResponse> getAllStations() {
        return stationService.getAllStations();
    }

    @PutMapping("/{code}/tanks/{tankCode}/price")
    public ResponseEntity<Void> updatePrice(
            @PathVariable String code,
            @PathVariable String tankCode,
            @Valid @RequestBody PriceUpdateRequest request) {

        stationService.updatePrice(code, tankCode, request.newPrice());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{code}/tanks")
    public List<TankResponse> getTanks(@PathVariable String code) {
        return stationService.getTanksByStation(code);
    }
}
