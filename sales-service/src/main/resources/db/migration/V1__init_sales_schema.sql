CREATE TABLE TRANSACTIONS (
                              id NUMBER GENERATED ALWAYS AS IDENTITY,
                              transaction_uuid VARCHAR2(50) NOT NULL,
                              station_code VARCHAR2(20) NOT NULL,
                              pump_number NUMBER NOT NULL,
                              nozzle_number NUMBER,
                              fuel_type VARCHAR2(30) NOT NULL,
                              volume NUMBER(10,2) NOT NULL,
                              unit_price NUMBER(10,2) NOT NULL,
                              total_amount NUMBER(10,2) NOT NULL,
                              plate_number VARCHAR2(20),
                              transaction_time TIMESTAMP NOT NULL,
                              status VARCHAR2(20) NOT NULL,
                              CONSTRAINT pk_transactions PRIMARY KEY (id)
)
    PARTITION BY RANGE (transaction_time) INTERVAL (NUMTOYMINTERVAL(1, 'MONTH'))
(
    PARTITION p_init VALUES LESS THAN (TO_DATE('2026-01-01', 'YYYY-MM-DD'))
);

-- Bütün partition'ları kapsayan Global Unique Index (Idempotency Garantisi)
CREATE UNIQUE INDEX idx_global_txn_uuid ON TRANSACTIONS(transaction_uuid) GLOBAL;

-- Raporlar için sadece ilgili aya (Local Partition) atanmış hızlı okuma indeksi
CREATE INDEX idx_txn_station_time ON TRANSACTIONS(station_code, transaction_time) LOCAL;