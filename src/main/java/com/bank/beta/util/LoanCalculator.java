package com.bank.beta.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class LoanCalculator {

    private static final int SCALE = 2;
    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);

    /**
     * Расчет аннуитетного платежа
     * Формула: П = (С × ПС) / (1 − (1 + ПС)^(−Срок))
     * где ПС - месячная процентная ставка
     */
    public BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal annualRate, int termMonths) {
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(termMonths), SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);
        BigDecimal ratePower = onePlusRate.pow(termMonths);

        BigDecimal annuityFactor = monthlyRate.multiply(ratePower)
                .divide(ratePower.subtract(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

        return amount.multiply(annuityFactor)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    public LocalDate calculateEndDate(LocalDate startDate, int termMonths) {
        return startDate.plusMonths(termMonths);
    }
}