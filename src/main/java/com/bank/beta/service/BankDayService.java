package com.bank.beta.service;

import com.bank.beta.dto.BankDayCloseDto;
import com.bank.beta.dto.OverdueStatsDto;
import com.bank.beta.entity.Loan;
import com.bank.beta.entity.LoanStatus;
import com.bank.beta.entity.PaymentSchedule;
import com.bank.beta.entity.PaymentStatus;
import com.bank.beta.repository.LoanRepository;
import com.bank.beta.repository.PaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankDayService {

    private final LoanRepository loanRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;

    @Transactional
    public BankDayCloseDto closeDay(LocalDate date) {
        log.info("Closing bank day for date: {}", date);

        // Находим все просроченные платежи
        List<PaymentSchedule> overduePayments = paymentScheduleRepository.findOverduePayments(date);

        // Обновляем статусы просроченных платежей
        List<BankDayCloseDto.OverdueNotificationDto> notifications = new ArrayList<>();

        for (PaymentSchedule payment : overduePayments) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.OVERDUE);
                paymentScheduleRepository.save(payment);

                // Обновляем статус кредита
                Loan loan = payment.getLoan();
                if (loan.getStatus() == LoanStatus.ACTIVE) {
                    loan.setStatus(LoanStatus.OVERDUE);
                    loanRepository.save(loan);
                }

                // Создаем уведомление
                BankDayCloseDto.OverdueNotificationDto notification = BankDayCloseDto.OverdueNotificationDto.builder()
                        .clientName(loan.getClient().getFullName())
                        .loanId(loan.getId().toString())
                        .daysOverdue(calculateDaysOverdue(payment.getDueDate(), date))
                        .overdueAmount(payment.getPlannedAmount().doubleValue())
                        .build();
                notifications.add(notification);
            }
        }

        // Пересчитываем проценты по всем активным кредитам
        recalculateAllLoans();

        log.info("Bank day closed. Found {} overdue payments", overduePayments.size());

        return BankDayCloseDto.builder()
                .closedDate(date)
                .processedLoans((int) loanRepository.count())
                .overdueLoans(overduePayments.size())
                .paymentsProcessed(overduePayments.size())
                .overdueNotifications(notifications)
                .build();
    }

    @Transactional
    public void recalculateAllLoans() {
        log.info("Recalculating all active loans");
        List<Loan> activeLoans = loanRepository.findAllActiveLoans();

        for (Loan loan : activeLoans) {
            recalculateLoan(loan);
        }

        log.info("Recalculated {} loans", activeLoans.size());
    }

    private void recalculateLoan(Loan loan) {
        // Пересчет процентов и пеней
        List<PaymentSchedule> pendingPayments = paymentScheduleRepository
                .findByLoanIdAndStatus(loan.getId(), PaymentStatus.PENDING);

        for (PaymentSchedule payment : pendingPayments) {
            if (payment.getDueDate().isBefore(LocalDate.now())) {
                // Начисляем пеню за просрочку
                BigDecimal penalty = calculatePenalty(payment);
                // TODO: добавить пеню к сумме платежа
            }
        }
    }

    public OverdueStatsDto getOverdueStats() {
        log.info("Getting overdue statistics");

        List<PaymentSchedule> overduePayments = paymentScheduleRepository
                .findOverduePayments(LocalDate.now());

        BigDecimal totalOverdueAmount = BigDecimal.ZERO;
        List<OverdueStatsDto.OverdueLoanDto> overdueLoans = new ArrayList<>();

        for (PaymentSchedule payment : overduePayments) {
            totalOverdueAmount = totalOverdueAmount.add(payment.getPlannedAmount());

            OverdueStatsDto.OverdueLoanDto overdueLoan = OverdueStatsDto.OverdueLoanDto.builder()
                    .loanId(payment.getLoan().getId())
                    .clientName(payment.getLoan().getClient().getFullName())
                    .overdueAmount(payment.getPlannedAmount())
                    .daysOverdue(calculateDaysOverdue(payment.getDueDate(), LocalDate.now()))
                    .monthlyPayment(payment.getLoan().getMonthlyPayment())
                    .build();
            overdueLoans.add(overdueLoan);
        }

        return OverdueStatsDto.builder()
                .totalOverdueLoans(overduePayments.size())
                .totalOverdueAmount(totalOverdueAmount)
                .overdueLoans(overdueLoans)
                .build();
    }

    private int calculateDaysOverdue(LocalDate dueDate, LocalDate currentDate) {
        return (int) dueDate.until(currentDate).getDays();
    }

    private BigDecimal calculatePenalty(PaymentSchedule payment) {
        int daysOverdue = calculateDaysOverdue(payment.getDueDate(), LocalDate.now());
        BigDecimal penaltyRate = new BigDecimal("0.001"); // 0.1%
        return payment.getPlannedAmount()
                .multiply(penaltyRate)
                .multiply(new BigDecimal(daysOverdue));
    }
}