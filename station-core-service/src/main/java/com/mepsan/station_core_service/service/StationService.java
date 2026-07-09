package com.mepsan.station_core_service.service;

import com.mepsan.station_core_service.dto.StationResponse;
import com.mepsan.station_core_service.dto.TankResponse;
import com.mepsan.station_core_service.entity.Station;
import com.mepsan.station_core_service.entity.Tank;
import com.mepsan.station_core_service.exception.StationNotFoundException;
import com.mepsan.station_core_service.exception.TankNotFoundException;
import com.mepsan.station_core_service.repository.StationRepository;
import com.mepsan.station_core_service.repository.TankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final TankRepository tankRepository;
    private final StringRedisTemplate redisTemplate;

    public List<StationResponse> getAllStations() {
        return stationRepository.findAll().stream().map(s -> new StationResponse(
                s.getStationCode(),
                s.getName(),
                s.getCity(),
                s.getStatus(),
                s.getTanks().stream().map(t -> new TankResponse(t.getTankCode(), t.getFuelType(), t.getCurrentPrice())).collect(Collectors.toList())
        )).collect(Collectors.toList());
    }

    @Transactional
    public void updatePrice(String stationCode, String tankCode, BigDecimal newPrice) {
        Tank tank = tankRepository.findByStation_StationCodeAndTankCode(stationCode, tankCode)
                .orElseThrow(() -> new TankNotFoundException(stationCode, tankCode));

        tank.setCurrentPrice(newPrice);
        tankRepository.save(tank);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisTemplate.opsForValue().set(
                                "price:" + stationCode + ":" + tank.getFuelType(),
                                newPrice.toString()
                        );
                    }
                }
        );
    }

    public List<TankResponse> getTanksByStation(String stationCode) {
        Station station = stationRepository.findByStationCode(stationCode)
                .orElseThrow(() -> new StationNotFoundException(stationCode));

        return tankRepository.findByStation(station).stream()
                .map(t -> new TankResponse(t.getTankCode(), t.getFuelType(), t.getCurrentPrice()))
                .collect(Collectors.toList());
    }
}
