package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EarlyPaymentCalculationDto {
    private UUID loanId;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal newRemainingDebt;
    private int monthsReduced;
    private BigDecimal totalInterestSaved;
    private List<ScheduleImpactDto> updatedSchedule;

    @Data
    @Builder
    public static class ScheduleImpactDto {
        private int paymentNumber;
        private LocalDate oldDueDate;
        private LocalDate newDueDate;
        private BigDecimal oldAmount;
        private BigDecimal newAmount;
    }
}