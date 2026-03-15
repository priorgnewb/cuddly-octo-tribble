package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class LoanSummaryDto {
    private UUID id;
    private BigDecimal amount;
    private BigDecimal remainingDebt;
    private BigDecimal monthlyPayment;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private int paymentsMade;
    private int totalPayments;
    private boolean hasOverdue;
}