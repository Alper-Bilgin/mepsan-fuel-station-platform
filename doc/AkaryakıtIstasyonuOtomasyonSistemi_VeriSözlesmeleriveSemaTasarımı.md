# Akaryakıt İstasyonu Otomasyon Sistemi — Veri Sözleşmeleri ve Şema Tasarımı (v1.0)

> Bu doküman, sistemin servisler arası iletişiminde kullanılacak JSON payload'larını, veritabanı şemalarını ve bu şemalar üzerinde alınan mimari kararları (partition stratejisi, kilitleme sırası, mutabakat formülü, cache invalidation) nihai haliyle içerir.

---

## 1. Servis Listesi ve Sorumluluklar

| Servis | Sorumluluk | Teknolojiler |
|---|---|---|
| iot-simulator-service | Donanım simülasyonu, test verisi üretimi | Java 17, Spring Boot |
| gateway-service | İstek karşılama, yetkilendirme, yönlendirme | Spring Boot, Spring Security |
| iot-ingestion-service | Veriyi hızla kuyruğa alma | Spring Boot, RabbitMQ |
| station-core-service | İstasyon/tank/pompa/fiyat/eşik tanımları | Oracle, Redis |
| sales-service | Satış işleme, kilitleme, kesin kayıt | Oracle, Redisson |
| telemetry-service | Saniyelik tank verisi, alarm, canlı takip | InfluxDB, Redis, WebSocket |
| epdk-reporting-service | Gece batch, mutabakat, yasal arşivleme | Spring Batch, MinIO |

---

## 2. RabbitMQ Mesaj Sözleşmeleri (JSON Payloads)

### 2.1 Satış Verisi (Sales Payload)
**Hedef:** `sales-exchange`
**Üretilme anı:** Tabanca pompaya geri konduğunda

```json
{
  "transactionId": "123e4567-e89b-12d3-a456-426614174000",
  "stationCode": "ST-KNY-001",
  "pumpNumber": 3,
  "nozzleNumber": 1,
  "fuelType": "DIESEL",
  "volume": 45.50,
  "unitPrice": 42.10,
  "totalAmount": 1915.55,
  "plateNumber": "42ABC123",
  "timestamp": "2026-07-04T14:45:00Z"
}
```

### 2.2 Telemetri Verisi (Telemetry Payload)
**Hedef:** `telemetry-exchange`
**Üretilme sıklığı:** Saniyede bir veya değişim oldukça

```json
{
  "stationCode": "ST-KNY-001",
  "tankId": "T-01",
  "fuelType": "DIESEL",
  "currentVolume": 14500.25,
  "temperature": 18.5,
  "waterLevel": 12.0,
  "timestamp": "2026-07-04T14:45:01Z"
}
```

> **Not:** `waterLevel` ve `temperature`, EPDK'nın kaçak/sızıntı tespitinde kullandığı en kritik metriklerdir.

### 2.3 (Gerekli Ek) Tanker İkmal Verisi (Refill Payload)
**Hedef:** `refill-exchange` *(mutabakat formülünün doğru çalışması için zorunlu)*
**Üretilme anı:** Tanker istasyona yakıt boşalttığında

```json
{
  "refillId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "stationCode": "ST-KNY-001",
  "tankId": "T-01",
  "fuelType": "DIESEL",
  "refillVolume": 8000.00,
  "supplierPlate": "34XYZ789",
  "timestamp": "2026-07-04T06:00:00Z"
}
```

---

## 3. Veritabanı Şemaları

### 3.1 Oracle — `station-core-service`

**STATIONS**
| Kolon | Tip | Açıklama |
|---|---|---|
| id | NUMBER (PK) | |
| station_code | VARCHAR (Unique) | Örn: ST-KNY-001 |
| name | VARCHAR | Örn: Mepsan Konya Merkez Bayi |
| city | VARCHAR | |
| status | VARCHAR | ACTIVE, SUSPENDED |

**TANKS**
| Kolon | Tip | Açıklama |
|---|---|---|
| id | NUMBER (PK) | |
| station_id | NUMBER (FK) | |
| tank_code | VARCHAR | Örn: T-01 |
| fuel_type | VARCHAR | |
| max_capacity | NUMBER | Litre |
| max_water_level | NUMBER | Alarm eşiği |
| max_temperature | NUMBER | Alarm eşiği |
| min_temperature | NUMBER | Alarm eşiği |

**PUMPS**
| Kolon | Tip | Açıklama |
|---|---|---|
| id | NUMBER (PK) | |
| station_id | NUMBER (FK) | |
| pump_number | NUMBER | |

**REFILLS** *(mutabakat için eklendi)*
| Kolon | Tip | Açıklama |
|---|---|---|
| id | NUMBER (PK) | |
| station_code | VARCHAR (Index) | |
| tank_code | VARCHAR | |
| refill_volume | NUMBER | |
| supplier_plate | VARCHAR | |
| refill_time | TIMESTAMP | |

---

### 3.2 Oracle — `sales-service`

**TRANSACTIONS**

> Microservice izolasyonu gereği bu servis istasyon detaylarını bilmez; sadece `station_code` üzerinden satışı kaydeder.

| Kolon | Tip | Açıklama |
|---|---|---|
| id | NUMBER (PK) | |
| transaction_uuid | VARCHAR (**Global Unique Index**) | Tekrarlı kayıt önleme |
| station_code | VARCHAR (Index) | |
| pump_number | NUMBER | |
| fuel_type | VARCHAR | |
| volume | NUMBER | |
| total_amount | NUMBER | |
| plate_number | VARCHAR | |
| transaction_time | TIMESTAMP | |
| status | VARCHAR | SUCCESS, REJECTED |

**Partition Stratejisi (Composite Partitioning):**
- **Ana bölümleme (Range):** `transaction_time` üzerinden aylık (`P_2026_07`, `P_2026_08`, ...)
- **Alt bölümleme (Hash Subpartition):** `station_code` üzerinden, aynı ay içindeki verinin diske dengeli dağılması için
- **Not:** `transaction_uuid` üzerindeki unique index **GLOBAL** olarak tanımlanmalı; local index tanımlanırsa uuid bazlı arama tüm partition'ları tarar.

---

### 3.3 Redis — `sales-service` & `station-core-service`

| Amaç | Key Formatı | Örnek | TTL |
|---|---|---|---|
| Fiyat Önbelleği | `price:{stationCode}:{fuelType}` | `price:ST-KNY-001:DIESEL` → `42.10` | Yok (write-through) |
| Dağıtık Kilit (Redisson) | `lock:pump:{stationCode}:{pumpNumber}` | `lock:pump:ST-KNY-001:3` | tryLock timeout (öneri: 3sn) |
| Alarm Eşiği Önbelleği | `thresholds:{tankCode}:{metric}` | `thresholds:T-01:water` | Write-through (station-core günceller) |

---

### 3.4 InfluxDB — `telemetry-service`

**Measurement:** `tank_telemetry_data`

| Tip | Alan | Örnek |
|---|---|---|
| Tag (indeksli) | station_code | ST-KNY-001 |
| Tag (indeksli) | tank_id | T-01 |
| Tag (indeksli) | fuel_type | DIESEL |
| Field (indekssiz) | volume | 14500.25 |
| Field (indekssiz) | temperature | 18.5 |
| Field (indekssiz) | water_level | 12.0 |
| Timestamp | — | Unix Epoch (ms) |

---

## 4. Kritik İş Akışı Kararları

### 4.1 Satış Onay Sırası (Race Condition Önlemi)
`sales-service` içindeki akış **kesinlikle** bu sırayla işletilir:

1. Redisson ile pompa bazlı kilidi al: `lock.tryLock("lock:pump:{stationCode}:{pumpNumber}", timeout)`
2. Kilit alınamazsa (timeout): mesaj dead-letter queue'ya alınır veya `REJECTED` + `LOCK_TIMEOUT` sebebiyle kaydedilir — asla sonsuz beklenmez.
3. Redis'ten güncel fiyatı oku: `price:{stationCode}:{fuelType}`
4. Payload'daki `unitPrice` ile Redis'teki fiyat karşılaştırılır.
    - Eşleşmiyorsa → `REJECTED` statüsüyle kaydet, işlemi bitir.
    - Eşleşiyorsa → Oracle'a `SUCCESS` statüsüyle yaz.
5. `finally` bloğunda kilit serbest bırakılır.

### 4.2 Fiyat Güncelleme — Write-Through Cache Akışı
Fiyat güncelleme yetkisi yalnızca `station-core-service`'tedir:

1. Yeni fiyat Oracle'a yazılır (transaction içinde).
2. **`AFTER_COMMIT` fazında** (`@TransactionalEventListener(phase = AFTER_COMMIT)`) Redis'teki `price:{stationCode}:{fuelType}` güncellenir.
3. Redis update başarısız olursa retry mekanizması devreye girer; kalıcı başarısızlıkta sales-service fallback olarak Oracle'a düşebilir.

> Not: Oracle yazımı ile Redis güncellemesi gerçek bir distributed transaction değildir; bu yüzden Redis update'i commit sonrasına alınmıştır.

### 4.3 Mutabakat Formülü (epdk-reporting-service — Gece İşi)
Her tank için günlük mutabakat:

$$\Delta V = (V_{start} + \text{TotalRefills}) - V_{end}$$

- `V_start`, `V_end`: InfluxDB'den günün ilk/son hacim ölçümleri
- `TotalRefills`: Oracle `REFILLS` tablosundan o gün o tank için toplam ikmal
- `TotalSales`: Oracle `TRANSACTIONS` tablosundan o gün o tank/yakıt için toplam satış

**Kural:** `ΔV`, `TotalSales`'e eşit olmalı. Fark, EPDK'nın izin verdiği binde 5 kalibrasyon toleransını aşarsa → **Kaçak/Sızıntı Alarmı** üretilir.

### 4.4 Alarm Eşikleri Yönetimi
- Eşik değerleri (`max_water_level`, `max_temperature`, `min_temperature`) `station-core-service`'teki `TANKS` tablosunda tutulur.
- Sistem ayağa kalktığında bu değerler Redis'e önbelleklenir (`thresholds:{tankCode}:{metric}`).
- `telemetry-service` her saniye Oracle'a gitmez, Redis'ten okur.
- Eşik güncellemesi de write-through cache prensibiyle senkron yapılır (bkz. 4.2).

---

## 5. Açık Kalan / İleride Netleştirilecek Noktalar

- [ ] Alarm üretimi ile alarm bildirimi (WebSocket push) arasındaki coupling netleştirilmeli.
- [ ] `tryLock` timeout süresi ve DLQ (dead-letter queue) davranışı kesinleştirilmeli.
- [ ] Refill verisinin kaynağı: IoT simülatörü mü üretecek, yoksa manuel/ERP entegrasyonu mu olacak?
- [ ] WebSocket bağlantı state'inin multi-instance ortamda paylaşımı (Redis pub/sub) tasarlanmalı.

---

*Doküman versiyonu: 1.0 — Alper Bilğin