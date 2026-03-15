package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PaymentHistoryDto {
    private List<PaymentResponseDto> payments;
    private int totalCount;
    private int page;
    private int size;
    private BigDecimal totalAmount;
    private BigDecimal totalPrincipal;
    private BigDecimal totalInterest;
}