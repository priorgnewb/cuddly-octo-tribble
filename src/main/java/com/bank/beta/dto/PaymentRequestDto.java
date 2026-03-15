package com.bank.beta.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "ID кредита обязателен")
    private String loanId;

    @NotNull(message = "Сумма платежа обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    @NotNull(message = "Дата платежа обязательна")
    @PastOrPresent(message = "Дата платежа не может быть в будущем")
    private LocalDate paymentDate;

    private boolean earlyPayment;  // true - досрочное погашение
}