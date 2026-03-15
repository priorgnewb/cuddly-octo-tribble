package com.bank.beta.controller;

import com.bank.beta.dto.BankDayCloseDto;
import com.bank.beta.dto.OverdueStatsDto;
import com.bank.beta.service.BankDayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/bank-day")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Операционный день", description = "Управление операционным днем банка")
public class BankDayController {

    private final BankDayService bankDayService;

    @PostMapping("/close")
    @Operation(summary = "Закрыть текущий день", description = "Закрывает операционный день и выполняет все необходимые расчеты")
    public ResponseEntity<BankDayCloseDto> closeCurrentDay() {
        log.info("POST /api/v1/bank-day/close - Closing current bank day");
        BankDayCloseDto response = bankDayService.closeDay(LocalDate.now());  // Передаем текущую дату
        return ResponseEntity.ok(response);
    }

    @PostMapping("/close/{date}")
    @Operation(summary = "Закрыть день за дату", description = "Закрывает операционный день за указанную дату")
    public ResponseEntity<BankDayCloseDto> closeDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("POST /api/v1/bank-day/close/{} - Closing bank day", date);
        BankDayCloseDto response = bankDayService.closeDay(date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    @Operation(summary = "Статистика просрочек", description = "Получает статистику по просроченным платежам")
    public ResponseEntity<OverdueStatsDto> getOverdueStats() {
        log.info("GET /api/v1/bank-day/overdue - Get overdue statistics");
        OverdueStatsDto response = bankDayService.getOverdueStats();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Пересчитать все кредиты", description = "Пересчитывает проценты и пени по всем активным кредитам")
    public ResponseEntity<String> recalculateAllLoans() {
        log.info("POST /api/v1/bank-day/recalculate - Recalculate all loans");
        bankDayService.recalculateAllLoans();
        return ResponseEntity.ok("All loans recalculated successfully");
    }
}