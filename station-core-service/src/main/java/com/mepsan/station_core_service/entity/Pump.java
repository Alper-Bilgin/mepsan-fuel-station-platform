package com.mepsan.station_core_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PUMPS")
@Getter @Setter
public class Pump {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "pump_number")
    private Integer pumpNumber;
}
