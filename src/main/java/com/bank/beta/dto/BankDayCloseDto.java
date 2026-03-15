package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class BankDayCloseDto {
    private LocalDate closedDate;
    private int processedLoans;
    private int overdueLoans;
    private int paymentsProcessed;
    private List<OverdueNotificationDto> overdueNotifications;

    @Data
    @Builder
    public static class OverdueNotificationDto {
        private String clientName;
        private String loanId;
        private int daysOverdue;
        private double overdueAmount;
    }
}