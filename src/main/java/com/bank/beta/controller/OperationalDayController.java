package com.bank.beta.controller;

import com.bank.beta.entity.OperationalDay;
import com.bank.beta.service.OperationalDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/operational-day")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Операционный день", description = "Управление операционным днем банка")
public class OperationalDayController {

    private final OperationalDayService operationalDayService;

    @PostMapping("/open")
    @Operation(summary = "Открыть операционный день", description = "Открывает текущий операционный день")
    public ResponseEntity<OperationalDay> openDay() {
        log.info("POST /api/v1/operational-day/open - Opening operational day");
        OperationalDay day = operationalDayService.openDay();
        return ResponseEntity.ok(day);
    }

    @PostMapping("/open/{date}")
    @Operation(summary = "Открыть операционный день за дату", description = "Открывает операционный день за указанную дату")
    public ResponseEntity<OperationalDay> openDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("POST /api/v1/operational-day/open/{} - Opening operational day", date);
        OperationalDay day = operationalDayService.openDay(date);
        return ResponseEntity.ok(day);
    }

    @PostMapping("/close")
    @Operation(summary = "Закрыть операционный день", description = "Закрывает текущий операционный день")
    public ResponseEntity<OperationalDay> closeDay() {
        log.info("POST /api/v1/operational-day/close - Closing operational day");
        OperationalDay day = operationalDayService.closeDay();
        return ResponseEntity.ok(day);
    }

    @PostMapping("/close/{date}")
    @Operation(summary = "Закрыть операционный день за дату", description = "Закрывает операционный день за указанную дату")
    public ResponseEntity<OperationalDay> closeDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("POST /api/v1/operational-day/close/{} - Closing operational day", date);
        OperationalDay day = operationalDayService.closeDay(date);
        return ResponseEntity.ok(day);
    }

    @GetMapping("/status")
    @Operation(summary = "Статус операционного дня", description = "Проверяет, открыт ли операционный день")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("GET /api/v1/operational-day/status - Getting operational day status");

        boolean isOpened = operationalDayService.isDayOpened();
        Map<String, Object> response = new HashMap<>();
        response.put("opened", isOpened);

        if (isOpened) {
            OperationalDay currentDay = operationalDayService.getCurrentDay();
            response.put("businessDate", currentDay.getBusinessDate());
            response.put("openedAt", currentDay.getOpenedAt());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{date}")
    @Operation(summary = "Статус за дату", description = "Проверяет статус операционного дня за указанную дату")
    public ResponseEntity<Map<String, Object>> getStatusForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /api/v1/operational-day/status/{} - Getting status for date", date);

        boolean isOpened = operationalDayService.isDayOpened(date);
        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("opened", isOpened);

        return ResponseEntity.ok(response);
    }
}