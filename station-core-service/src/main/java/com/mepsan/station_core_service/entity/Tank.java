package com.mepsan.station_core_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "TANKS")
@Getter @Setter
public class Tank {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "tank_code")
    private String tankCode;

    @Column(name = "fuel_type")
    private String fuelType;

    // EKLENEN KISIM: columnDefinition = "NUMBER"
    @Column(name = "max_capacity", columnDefinition = "NUMBER")
    private Double maxCapacity;

    @Column(name = "max_water_level", columnDefinition = "NUMBER")
    private Double maxWaterLevel;

    @Column(name = "max_temperature", columnDefinition = "NUMBER")
    private Double maxTemperature;

    @Column(name = "min_temperature", columnDefinition = "NUMBER")
    private Double minTemperature;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Version
    private Long version;
}
