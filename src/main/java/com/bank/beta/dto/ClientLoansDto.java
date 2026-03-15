package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ClientLoansDto {
    private UUID clientId;
    private String clientFullName;
    private List<LoanSummaryDto> activeLoans;
    private List<LoanSummaryDto> closedLoans;
    private BigDecimal totalDebt;
    private BigDecimal totalPaid;
    private int activeLoansCount;
    private int closedLoansCount;
}