package com.mepsan.iot_simulator_service.controller;

import com.mepsan.iot_simulator_service.scheduler.SimulationScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationScheduler scheduler;

    @PostMapping("/start")
    public String start() {
        scheduler.setRunning(true);
        return "Simulasyon Baslatildi! RabbitMQ hedefleri bombalaniyor...";
    }

    @PostMapping("/stop")
    public String stop() {
        scheduler.setRunning(false);
        return "Simulasyon Durduruldu.";
    }

    @GetMapping("/status")
    public String status() {
        return scheduler.isRunning() ? "Motor Calisiyor (RUNNING)" : "Motor Beklemede (STOPPED)";
    }
}
