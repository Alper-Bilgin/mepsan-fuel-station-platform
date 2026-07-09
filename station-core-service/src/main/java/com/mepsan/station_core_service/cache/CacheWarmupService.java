package com.mepsan.station_core_service.cache;

import com.mepsan.station_core_service.entity.Pump;
import com.mepsan.station_core_service.entity.Station;
import com.mepsan.station_core_service.entity.Tank;
import com.mepsan.station_core_service.repository.StationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupService {

    private final StationRepository stationRepository;
    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    public void warmupCache() {
        List<Station> stations = stationRepository.findAllByStatus("ACTIVE");
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        ValueOperations<String, String> valOps = redisTemplate.opsForValue();

        for (Station station : stations) {
            String sCode = station.getStationCode();
            setOps.add("valid:stations", sCode);

            for (Pump pump : station.getPumps()) {
                setOps.add("valid:pumps:" + sCode, String.valueOf(pump.getPumpNumber()));
            }

            for (Tank tank : station.getTanks()) {
                valOps.set("price:" + sCode + ":" + tank.getFuelType(), tank.getCurrentPrice().toString());

                String tankPrefix = "thresholds:" + sCode + ":" + tank.getTankCode();
                valOps.set(tankPrefix + ":water", String.valueOf(tank.getMaxWaterLevel()));
                valOps.set(tankPrefix + ":maxTemp", String.valueOf(tank.getMaxTemperature()));
                valOps.set(tankPrefix + ":minTemp", String.valueOf(tank.getMinTemperature()));
            }
        }
        log.info("Redis cache warmup tamamlandi: {} istasyon yuklendi", stations.size());
    }
}
