package com.mepsan.station_core_service.repository;

import com.mepsan.station_core_service.entity.Station;
import com.mepsan.station_core_service.entity.Tank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TankRepository extends JpaRepository<Tank, Long> {
    List<Tank> findByStation(Station station);
    Optional<Tank> findByStation_StationCodeAndTankCode(String stationCode, String tankCode);
}
