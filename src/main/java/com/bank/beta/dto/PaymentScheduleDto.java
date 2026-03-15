package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PaymentScheduleDto {
    private UUID loanId;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private int totalPayments;
    private int paidPayments;
    private List<SchedulePaymentDto> payments;

    @Data
    @Builder
    public static class SchedulePaymentDto {
        private int number;
        private LocalDate dueDate;
        private BigDecimal plannedAmount;
        private BigDecimal actualAmount;
        private String status;
        private BigDecimal principalPart;
        private BigDecimal interestPart;
        private LocalDate paidDate;
        private boolean isOverdue;
        private int daysOverdue;
    }
}