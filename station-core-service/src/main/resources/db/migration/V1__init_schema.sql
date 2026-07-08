CREATE TABLE STATIONS (
                          id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          station_code VARCHAR2(20) NOT NULL UNIQUE,
                          name VARCHAR2(200) NOT NULL,
                          city VARCHAR2(100),
                          status VARCHAR2(20) DEFAULT 'ACTIVE' NOT NULL,
                          version NUMBER DEFAULT 0 NOT NULL
);

CREATE TABLE TANKS (
                       id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       station_id NUMBER NOT NULL REFERENCES STATIONS(id),
                       tank_code VARCHAR2(20) NOT NULL,
                       fuel_type VARCHAR2(30) NOT NULL,
                       max_capacity NUMBER NOT NULL,
                       max_water_level NUMBER DEFAULT 5,
                       max_temperature NUMBER DEFAULT 30,
                       min_temperature NUMBER DEFAULT 5,
                       current_price NUMBER(10,2) NOT NULL,
                       version NUMBER DEFAULT 0 NOT NULL,
                       CONSTRAINT uq_station_tank UNIQUE (station_id, tank_code)
);

CREATE TABLE PUMPS (
                       id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       station_id NUMBER NOT NULL REFERENCES STATIONS(id),
                       pump_number NUMBER NOT NULL,
                       CONSTRAINT uq_station_pump UNIQUE (station_id, pump_number)
);

CREATE TABLE REFILLS (
                         id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         refill_id VARCHAR2(50) NOT NULL UNIQUE,
                         station_code VARCHAR2(20) NOT NULL,
                         tank_code VARCHAR2(20) NOT NULL,
                         fuel_type VARCHAR2(30) NOT NULL,
                         refill_volume NUMBER NOT NULL,
                         supplier_plate VARCHAR2(20),
                         refill_time TIMESTAMP NOT NULL
);

CREATE INDEX idx_refills_station_time ON REFILLS(station_code, refill_time);
CREATE INDEX idx_tanks_station_id ON TANKS(station_id);
CREATE INDEX idx_pumps_station_id ON PUMPS(station_id);