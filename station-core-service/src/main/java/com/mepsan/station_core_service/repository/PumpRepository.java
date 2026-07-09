package com.mepsan.station_core_service.repository;

import com.mepsan.station_core_service.entity.Pump;
import com.mepsan.station_core_service.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PumpRepository extends JpaRepository<Pump, Long> {
    List<Pump> findByStation(Station station);
}
