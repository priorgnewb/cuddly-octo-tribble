package com.bank.beta.repository;

import com.bank.beta.entity.PaymentSchedule;
import com.bank.beta.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, UUID> {

    List<PaymentSchedule> findByLoanIdOrderByPaymentNumberAsc(UUID loanId);

    List<PaymentSchedule> findByLoanIdAndStatus(UUID loanId, PaymentStatus status);

    @Query("SELECT ps FROM PaymentSchedule ps WHERE ps.dueDate < :currentDate AND ps.status = 'PENDING'")
    List<PaymentSchedule> findOverduePayments(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT ps FROM PaymentSchedule ps WHERE ps.loan.id = :loanId AND ps.status = 'PENDING' ORDER BY ps.paymentNumber ASC")
    List<PaymentSchedule> findPendingPayments(@Param("loanId") UUID loanId);
}