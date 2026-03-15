package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OverdueStatsDto {
    private int totalOverdueLoans;
    private BigDecimal totalOverdueAmount;
    private List<OverdueLoanDto> overdueLoans;

    @Data
    @Builder
    public static class OverdueLoanDto {
        private UUID loanId;
        private String clientName;
        private BigDecimal overdueAmount;
        private int daysOverdue;
        private BigDecimal monthlyPayment;
    }
}