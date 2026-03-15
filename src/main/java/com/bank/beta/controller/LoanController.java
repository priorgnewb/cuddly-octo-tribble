package com.bank.beta.controller;

import com.bank.beta.dto.*;
import com.bank.beta.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Кредиты", description = "Управление кредитами")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Создание кредита", description = "Создает новый кредит для клиента. Если clientId не указан или не найден, создает нового клиента автоматически")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Кредит успешно создан",
                    content = @Content(schema = @Schema(implementation = LoanResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")
    })
    public ResponseEntity<LoanResponseDto> createLoan(@Valid @RequestBody LoanRequestDto request) {
        log.info("POST /api/v1/loans - Create loan");
        LoanResponseDto response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{loanId}")
    @Operation(summary = "Детали кредита", description = "Получает полную информацию о кредите включая график платежей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Кредит найден"),
            @ApiResponse(responseCode = "404", description = "Кредит не найден")
    })
    public ResponseEntity<LoanDetailDto> getLoanDetails(
            @Parameter(description = "ID кредита", required = true)
            @PathVariable UUID loanId) {
        log.info("GET /api/v1/loans/{} - Get loan details", loanId);
        LoanDetailDto response = loanService.getLoanDetails(loanId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Кредиты клиента", description = "Получает список всех кредитов клиента")
    public ResponseEntity<List<LoanSummaryDto>> getClientLoans(
            @Parameter(description = "ID клиента", required = true)
            @PathVariable UUID clientId) {
        log.info("GET /api/v1/loans/client/{} - Get client loans", clientId);
        List<LoanSummaryDto> response = loanService.getClientLoans(clientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loanId}/schedule")
    @Operation(summary = "График платежей", description = "Получает детальный график платежей по кредиту")
    public ResponseEntity<PaymentScheduleDto> getPaymentSchedule(
            @Parameter(description = "ID кредита", required = true)
            @PathVariable UUID loanId) {
        log.info("GET /api/v1/loans/{}/schedule - Get payment schedule", loanId);
        PaymentScheduleDto response = loanService.getPaymentSchedule(loanId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{loanId}/remaining-debt")
    @Operation(summary = "Остаток долга", description = "Получает информацию об остатке долга по кредиту")
    public ResponseEntity<RemainingDebtDto> getRemainingDebt(
            @Parameter(description = "ID кредита", required = true)
            @PathVariable UUID loanId) {
        log.info("GET /api/v1/loans/{}/remaining-debt - Get remaining debt", loanId);
        RemainingDebtDto response = loanService.getRemainingDebt(loanId);
        return ResponseEntity.ok(response);
    }
}