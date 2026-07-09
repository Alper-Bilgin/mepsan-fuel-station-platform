package com.mepsan.sales_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "TRANSACTIONS")
@Getter @Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transactions_seq")
    @SequenceGenerator(name = "transactions_seq", sequenceName = "transactions_seq", allocationSize = 1)
    private Long id;

    @Column(name = "transaction_uuid", unique = true)
    private String transactionUuid;

    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "pump_number")
    private Integer pumpNumber;

    @Column(name = "nozzle_number")
    private Integer nozzleNumber;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(columnDefinition = "NUMBER")
    private BigDecimal volume;

    @Column(name = "unit_price", columnDefinition = "NUMBER")
    private BigDecimal unitPrice;

    @Column(name = "total_amount", columnDefinition = "NUMBER")
    private BigDecimal totalAmount;

    @Column(name = "plate_number")
    private String plateNumber;

    @Column(name = "transaction_time")
    private Instant transactionTime;

    private String status;
}
