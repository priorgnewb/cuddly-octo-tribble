package com.bank.beta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание кредита")
public class LoanRequestDto {

    @Schema(description = "ID клиента (если null - создастся новый)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String clientId;

    @Schema(description = "ФИО клиента (для нового клиента)", example = "Иванов Иван Петрович")
    private String fullName;

    @Schema(description = "Номер паспорта (для нового клиента)", example = "AB1234567")
    private String passportNumber;

    @Schema(description = "Номер телефона (для нового клиента)", example = "+79001234567")
    private String phoneNumber;

    @Schema(description = "Email (для нового клиента)", example = "ivan.ivanov@email.com")
    private String email;

    @Schema(description = "Сумма кредита", example = "1000000.00", required = true)
    @NotNull
    @Positive
    private BigDecimal amount;

    @Schema(description = "Годовая процентная ставка", example = "15.5", required = true)
    @NotNull
    @DecimalMin("0.1")
    @DecimalMax("100.0")
    private BigDecimal interestRate;

    @Schema(description = "Срок кредита в месяцах", example = "12", required = true)
    @NotNull
    @Min(1)
    @Max(360)
    private Integer termMonths;

    @Schema(description = "Дата начала кредита", example = "2026-03-15", required = true)
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;
}