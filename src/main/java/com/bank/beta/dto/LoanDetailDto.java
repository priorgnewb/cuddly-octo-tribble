package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LoanDetailDto {
    private UUID loanId;
    private BigDecimal totalAmount;
    private BigDecimal remainingDebt;
    private BigDecimal monthlyPayment;
    private String status;
    private List<ScheduleItemDto> paymentSchedule;

    @Data
    @Builder
    public static class ScheduleItemDto {
        private Integer paymentNumber;
        private LocalDate dueDate;
        private BigDecimal plannedAmount;
        private BigDecimal actualAmount;
        private String status;
        private LocalDate paidDate;
    }
}