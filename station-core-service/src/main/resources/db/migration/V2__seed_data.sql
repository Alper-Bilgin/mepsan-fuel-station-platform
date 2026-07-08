-- 50 istasyon
INSERT INTO STATIONS (station_code, name, city, status)
SELECT 'ST-KNY-' || LPAD(LEVEL, 3, '0'), 'Mepsan Konya Bayi ' || LEVEL, 'Konya', 'ACTIVE'
FROM DUAL CONNECT BY LEVEL <= 50;

-- Her istasyona T-01/DIESEL, T-02/GASOLINE, T-03/LPG
INSERT INTO TANKS (station_id, tank_code, fuel_type, max_capacity, current_price)
SELECT s.id, 'T-01', 'DIESEL', 30000, 42.10 FROM STATIONS s;
INSERT INTO TANKS (station_id, tank_code, fuel_type, max_capacity, current_price)
SELECT s.id, 'T-02', 'GASOLINE', 25000, 45.30 FROM STATIONS s;
INSERT INTO TANKS (station_id, tank_code, fuel_type, max_capacity, current_price)
SELECT s.id, 'T-03', 'LPG', 15000, 24.80 FROM STATIONS s;

-- DÜZELTİLEN KISIM: Cross Join ile temiz pompa dağıtımı
INSERT INTO PUMPS (station_id, pump_number)
SELECT s.id, p.pump_number
FROM STATIONS s
         CROSS JOIN (SELECT LEVEL AS pump_number FROM DUAL CONNECT BY LEVEL <= 8) p;