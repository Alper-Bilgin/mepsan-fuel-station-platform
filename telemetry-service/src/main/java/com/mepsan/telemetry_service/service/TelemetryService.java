package com.mepsan.telemetry_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.mepsan.telemetry_service.dto.AlarmEvent;
import com.mepsan.telemetry_service.dto.TelemetryMessage;
import com.mepsan.telemetry_service.dto.WsEnvelope;
import com.mepsan.telemetry_service.websocket.TelemetryWebSocketHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final InfluxDBClient influxDBClient;
    private final StringRedisTemplate redisTemplate;
    private final TelemetryWebSocketHandler webSocketHandler;

    // Her çağrıda üretmek yerine bir kere üretip bellekte tutuyoruz
    private WriteApiBlocking writeApi;

    @PostConstruct
    public void init() {
        this.writeApi = influxDBClient.getWriteApiBlocking();
    }

    public void processTelemetry(TelemetryMessage msg) {
        // 1. InfluxDB'ye ham veriyi yaz
        writeToInflux(msg);

        // 2. Redis üzerinden eşik kontrollerini yap (Hata fırlatmaz, sessizce fail olur)
        checkThresholds(msg);

        // 3. Normal veri takibi için WebSocket yayını yap (Envelope ile sarılı)
        webSocketHandler.broadcast(new WsEnvelope("TELEMETRY", msg));
    }

    private void writeToInflux(TelemetryMessage msg) {
        try {
            Point point = Point.measurement("tank_telemetry_data")
                    .addTag("station_code", msg.stationCode())
                    .addTag("tank_id", msg.tankId())
                    .addTag("fuel_type", msg.fuelType())
                    .addField("volume", msg.currentVolume())
                    .addField("temperature", msg.temperature())
                    .addField("water_level", msg.waterLevel())
                    .time(Instant.parse(msg.timestamp()), WritePrecision.MS);

            writeApi.writePoint(point);
        } catch (Exception e) {
            log.error("InfluxDB yazma hatasi: {}", e.getMessage());
        }
    }

    private void checkThresholds(TelemetryMessage msg) {
        String prefix = "thresholds:" + msg.stationCode() + ":" + msg.tankId();
        try {
            String maxWaterStr = redisTemplate.opsForValue().get(prefix + ":water");
            String maxTempStr = redisTemplate.opsForValue().get(prefix + ":maxTemp");
            String minTempStr = redisTemplate.opsForValue().get(prefix + ":minTemp");

            if (maxWaterStr == null || maxTempStr == null || minTempStr == null) {
                log.debug("Esik degerleri bulunamadi (Redis'te yok): {}", prefix);
                return;
            }

            double maxWater = Double.parseDouble(maxWaterStr);
            double maxTemp = Double.parseDouble(maxTempStr);
            double minTemp = Double.parseDouble(minTempStr);

            if (msg.waterLevel() > maxWater) raiseAlarm(msg, "WATER_LEVEL_HIGH", msg.waterLevel(), maxWater);
            if (msg.temperature() > maxTemp) raiseAlarm(msg, "TEMP_HIGH", msg.temperature(), maxTemp);
            if (msg.temperature() < minTemp) raiseAlarm(msg, "TEMP_LOW", msg.temperature(), minTemp);

        } catch (Exception e) {
            // Redis bağlantı hatası (ConnectionFailureException) vb. durumlarda consumer'ı patlatmamak için
            log.error("Esik kontrolu sirasinda beklenmeyen hata: {}", e.getMessage());
        }
    }

    private void raiseAlarm(TelemetryMessage msg, String type, double actual, double threshold) {
        AlarmEvent alarm = new AlarmEvent(
                msg.stationCode(), msg.tankId(), type, actual, threshold, msg.timestamp()
        );
        log.warn("ALARM! {} - Istasyon: {}, Tank: {}, Deger: {}, Esik: {}",
                type, msg.stationCode(), msg.tankId(), actual, threshold);

        // Alarmı Envelope ile sarıp gönderiyoruz
        webSocketHandler.broadcast(new WsEnvelope("ALARM", alarm));
    }
}