package com.bank.beta.config;

import com.bank.beta.service.OperationalDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DayInitializer {

    private final OperationalDayService operationalDayService;

    @EventListener(ApplicationReadyEvent.class)
    public void openDayOnStartup() {
        log.info("===== Checking operational day on application startup =====");
        try {
            if (!operationalDayService.isDayOpened()) {
                log.info("Operational day is not opened. Opening now...");
                operationalDayService.openDay();
                log.info("Operational day opened successfully on startup");
            } else {
                log.info("Operational day is already opened. Continuing...");
                log.info("Current business date: {}", operationalDayService.getCurrentDay().getBusinessDate());
            }
        } catch (Exception e) {
            log.error("Failed to open operational day on startup: {}", e.getMessage(), e);
        }
        log.info("===== Startup day check completed =====");
    }
}