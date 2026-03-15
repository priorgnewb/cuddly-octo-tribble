package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class RemainingDebtDto {
    private UUID loanId;
    private BigDecimal remainingDebt;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal nextPaymentAmount;
    private LocalDate nextPaymentDate;
    private int remainingPayments;
    private String status;
    private boolean isOverdue;
    private BigDecimal overdueAmount;
}