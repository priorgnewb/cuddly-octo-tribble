package com.bank.beta.controller;

import com.bank.beta.dto.*;
import com.bank.beta.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Платежи", description = "Управление платежами по кредитам")
public class PaymentController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Внесение платежа", description = "Вносит платеж по кредиту")
    public ResponseEntity<PaymentResponseDto> makePayment(@Valid @RequestBody PaymentRequestDto request) {
        log.info("POST /api/v1/payments - Make payment for loan: {}, amount: {}",
                request.getLoanId(), request.getAmount());
        PaymentResponseDto response = loanService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/early/calculate")
    @Operation(summary = "Расчет досрочного погашения", description = "Рассчитывает параметры досрочного погашения")
    public ResponseEntity<EarlyPaymentCalculationDto> calculateEarlyPayment(
            @RequestParam UUID loanId,
            @RequestParam BigDecimal amount,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate) {
        log.info("POST /api/v1/payments/early/calculate - Calculate early payment for loan: {}", loanId);
        EarlyPaymentCalculationDto response = loanService.calculateEarlyPayment(loanId, amount, paymentDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/early")
    @Operation(summary = "Досрочное погашение", description = "Вносит досрочный платеж с пересчетом графика")
    public ResponseEntity<PaymentResponseDto> makeEarlyPayment(@Valid @RequestBody EarlyPaymentRequestDto request) {
        log.info("POST /api/v1/payments/early - Make early payment for loan: {}", request.getLoanId());
        PaymentResponseDto response = loanService.processEarlyPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/loan/{loanId}")
    @Operation(summary = "История платежей по кредиту", description = "Получает все платежи по конкретному кредиту")
    public ResponseEntity<List<PaymentResponseDto>> getLoanPayments(
            @Parameter(description = "ID кредита", required = true)
            @PathVariable UUID loanId) {
        log.info("GET /api/v1/payments/loan/{} - Get payment history", loanId);
        List<PaymentResponseDto> response = loanService.getLoanPayments(loanId);
        return ResponseEntity.ok(response);
    }
}