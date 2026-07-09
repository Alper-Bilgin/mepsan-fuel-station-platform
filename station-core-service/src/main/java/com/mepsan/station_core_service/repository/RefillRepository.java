package com.mepsan.station_core_service.repository;

import com.mepsan.station_core_service.entity.Refill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefillRepository extends JpaRepository<Refill, Long> {
    boolean existsByRefillId(String refillId);
}
