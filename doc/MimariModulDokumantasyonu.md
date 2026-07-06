# Mimari Modül Dokümantasyonu

## İçindekiler
1. [API Gateway Modülü](#1-api-gateway-modülü-gateway-service)
2. [IoT Veri Toplama Modülü](#2-iot-veri-toplama-modülü-iot-ingestion-service)
3. [Ana Yönetim Modülü](#3-ana-yönetim-modülü-station-core-service)
4. [Satış ve Finans Modülü](#4-satış-ve-finans-modülü-sales-service)
5. [Telemetri ve İzleme Modülü](#5-telemetri-ve-i̇zleme-modülü-telemetry-service)
6. [EPDK Raporlama ve Arşiv Modülü](#6-epdk-raporlama-ve-arşiv-modülü-epdk-reporting-service)
7. [Veri Simülasyon Modülü](#7-veri-simülasyon-modülü-iot-simulator-service)
8. [Tam Mimari Özeti](#güncellenmiş-tam-mimari-özeti)

---

## 1. API Gateway Modülü (`gateway-service`)

Sistemin dış dünyaya açılan tek kapısıdır. İstasyonlardan gelen veya frontend/dashboard tarafından yapılan tüm istekler buradan geçer.

- **Teknoloji:** Spring Cloud Gateway

### Sorumluluklar
- Güvenlik ve Kimlik Doğrulama (JWT Token validasyonu)
- Rate Limiting (Örneğin bir istasyonun saniyede gönderebileceği veri limitini belirleme)
- İstekleri doğru alt servislere yönlendirme (Routing)

---

## 2. IoT Veri Toplama Modülü (`iot-ingestion-service`)

Donanımlardan (pompalar ve tank sensörleri) gelen anlık verilerin doğrudan karşılandığı, son derece hafif ve hızlı olması gereken modüldür.

- **Teknoloji:** Spring Boot, RabbitMQ

### Sorumluluklar
- Hiçbir veritabanına doğrudan bağlanmaz (hız kaybı yaşamamak için)
- Gelen JSON verilerini alır, yapısal doğruluğunu kontrol eder ve anında RabbitMQ'daki ilgili exchange/kuyruklara (`sales-exchange`, `telemetry-exchange`) fırlatır

---

## 3. Ana Yönetim Modülü (`station-core-service`)

Sistemin beynidir. İstasyonların, pompaların, bayilerin ve şirket verilerinin yönetildiği CRUD ağırlıklı servistir.

- **Teknoloji:** Spring Boot, Oracle DB, Redis

### Sorumluluklar
- **Oracle DB:** İstasyon ve donanım tanımlamalarını, bayi bilgilerini kalıcı olarak saklar
- **Redis:** Akaryakıt fiyatlarını ve istasyonların "Aktif/Pasif" durumlarını bellekte (cache) tutarak, fiyat soran binlerce pompanın saniyelik okuma isteklerine milisaniyeler içinde cevap verir

---

## 4. Satış ve Finans Modülü (`sales-service`)

Olay güdümlü mimarinin (Event-Driven) en kritik tüketicisidir (consumer). Satış verilerini işler ve kesinleştirir.

- **Teknoloji:** Spring Boot, RabbitMQ, Oracle DB, Redis (Redisson)

### Sorumluluklar
- RabbitMQ'daki `sales-queue` kuyruğunu dinler
- Fiyat tutarlılığını kontrol eder ve başarılı satışları Oracle DB'deki `transactions` tablosuna ACID kurallarına uyarak kaydeder
- **Redis Distributed Lock (Dağıtık Kilit):** Aynı pompa ID'sinden (örneğin ağ gecikmesi nedeniyle) aynı anda iki satış isteği gelirse, Redisson ile kilitleme yaparak mükerrer kaydı (double-spending) engeller

---

## 5. Telemetri ve İzleme Modülü (`telemetry-service`)

Tanklardan gelen milimetrik yakıt seviyelerini, sıcaklık sensörlerini ve sızıntı alarmlarını yönetir.

- **Teknoloji:** Spring Boot, RabbitMQ, InfluxDB, WebSockets

### Sorumluluklar
- RabbitMQ'daki `telemetry-queue` kuyruğunu dinler
- Zaman serisi formatındaki yüksek hacimli verileri InfluxDB'ye yazar
- Tanklarda ani düşüş (kaçak/sızıntı) algılarsa bir alarm eventi üretir
- İstasyonların anlık durumlarının takip edilebilmesi (`live-asset-tracking`) için WebSockets üzerinden frontend paneline canlı veri fırlatır

---

## 6. EPDK Raporlama ve Arşiv Modülü (`epdk-reporting-service`)

Sistemin yasal yükümlülüklerini yerine getirdiği, arka plan işlerinin (background jobs) koştuğu modüldür.

- **Teknoloji:** Spring Boot, Spring Batch, Oracle DB, MinIO

### Sorumluluklar
- **Spring Batch:** Her gece 23:59'da tetiklenerek gün boyunca Oracle'da biriken satış verilerini ve InfluxDB'den alınan gün sonu tank seviyelerini birleştirerek mutabakat yapar
- İstenilen formata göre (XML/JSON) dosyaları oluşturur, imzalar ve EPDK servislerine gönderir
- **MinIO:** Oluşturulan bu yasal rapor dosyalarını diske kaydeder, böylece yıllar sonra bile raporlara dosya yolu üzerinden anında ulaşılabilir

---

## 7. Veri Simülasyon Modülü (`iot-simulator-service`)

Bu servis, gerçek dünyadaki binlerce Mepsan pompasını ve tank otomasyon cihazını taklit eden bağımsız bir veri üreticisidir (Producer).

- **Teknoloji:** Java 17, Spring Boot, RabbitMQ (AMQP), Datafaker (veya Java Faker)

### Sorumluluklar
- **Eşzamanlılık (Concurrency):** `ExecutorService` veya `@Scheduled` yapıları kullanılarak aynı anda yüzlerce thread (iş parçacığı) ayağa kaldırılır. Her bir thread bir akaryakıt istasyonunu taklit eder
- **Satış Simülasyonu:** Datafaker kullanılarak rastgele plakalar, 10-50 litre arası rastgele dolum miktarları ve güncel pompa fiyatları üzerinden JSON formatında satış verileri (payload) oluşturulur ve RabbitMQ'daki `sales-exchange`'e fırlatılır
- **Telemetri Simülasyonu:** Tanklardaki yakıt seviyesinin satıldıkça azalmasını matematiksel olarak taklit eder. Saniyede bir tankın güncel hacmini ve sıcaklığını hesaplayıp RabbitMQ'daki `telemetry-exchange`'e gönderir
- **Dışa Bağımlılık:** Sadece RabbitMQ'yu tanır. Oracle veya InfluxDB ile hiçbir bağlantısı yoktur. Tıpkı gerçek bir donanım gibi veriyi kuyruğa atar ve işin gerisine karışmaz

---

## Tam Mimari Özeti

1. `iot-simulator-service`: (Test motoru) Donanım gibi davranıp sürekli veri fırlatır
2. `gateway-service`: İstekleri karşılar, yetkilendirir ve yönlendirir
3. `iot-ingestion-service`: Veriyi Gateway'den alıp hızla RabbitMQ kuyruklarına bırakır
4. `station-core-service`: İstasyon/fiyat tanımlamalarını (Oracle + Redis) yönetir
5. `sales-service`: Satış verilerini işler, kilitleme (Redisson) yapar ve kesin satışları Oracle'a yazar
6. `telemetry-service`: Saniyelik tank verilerini InfluxDB'ye yazar, alarmları ve canlı takibi (WebSocket) yönetir
7. `epdk-reporting-service`: Gece çalışarak (Spring Batch) yasal verileri MinIO'ya arşivler ve kuruma gönderir