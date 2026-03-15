package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponseDto {
    private UUID id;
    private UUID loanId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String type;
    private BigDecimal principalPart;
    private BigDecimal interestPart;
    private Integer paymentNumber;
    private LocalDateTime createdAt;
    private BigDecimal remainingDebtAfter;
    private String status;
}