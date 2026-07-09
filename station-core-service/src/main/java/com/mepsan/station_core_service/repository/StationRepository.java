package com.mepsan.station_core_service.repository;

import com.mepsan.station_core_service.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByStationCode(String stationCode);
    List<Station> findAllByStatus(String status);
}
