package com.bank.beta.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSchedule {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private Integer paymentNumber;  // Номер платежа по порядку

    @Column(nullable = false)
    private LocalDate dueDate;      // Плановая дата платежа

    @Column(nullable = false)
    private BigDecimal plannedAmount;  // Плановая сумма

    private BigDecimal actualAmount;   // Фактическая сумма (если оплатили)

    private LocalDate paidDate;        // Дата фактической оплаты

    private BigDecimal principalPart;  // Часть в основной долг
    private BigDecimal interestPart;   // Часть в проценты

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;      // Статус платежа

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }
}