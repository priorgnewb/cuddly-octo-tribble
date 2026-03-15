package com.bank.beta.scheduler;

import com.bank.beta.service.OperationalDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DayScheduler {

    private final OperationalDayService operationalDayService;

    // Проверяем каждый час
    @Scheduled(cron = "0 0 * * * *")
    public void ensureDayOpened() {
        log.info("Scheduler: Ensuring operational day is opened");
        try {
            operationalDayService.ensureDayOpened();
            log.info("Day status confirmed: OPENED");
        } catch (Exception e) {
            log.error("Failed to ensure day opened: {}", e.getMessage(), e);
        }
    }

    // Дополнительная проверка каждые 5 минут в рабочее время
    @Scheduled(cron = "0 */5 8-20 * * *")
    public void frequentCheckDuringBusinessHours() {
        log.debug("Quick check: Operational day status");
        if (!operationalDayService.isDayOpened()) {
            log.warn("Day is not opened during business hours! Attempting to open...");
            try {
                operationalDayService.openDay();
                log.info("Day opened successfully during business hours check");
            } catch (Exception e) {
                log.error("Failed to open day during business hours: {}", e);
            }
        }
    }
}