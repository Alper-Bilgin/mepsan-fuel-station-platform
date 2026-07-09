package com.mepsan.station_core_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "REFILLS")
@Getter @Setter
public class Refill {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refills_seq")
    @SequenceGenerator(name = "refills_seq", sequenceName = "refills_seq", allocationSize = 1)
    private Long id;

    @Column(name = "refill_id", unique = true)
    private String refillId;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "tank_code")
    private String tankCode;

    @Column(name = "fuel_type")
    private String fuelType;

    // EKLENEN KISIM: columnDefinition = "NUMBER"
    @Column(name = "refill_volume", columnDefinition = "NUMBER")
    private Double refillVolume;

    @Column(name = "supplier_plate")
    private String supplierPlate;

    @Column(name = "refill_time")
    private Instant refillTime;
}
