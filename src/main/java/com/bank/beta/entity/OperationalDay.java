package com.bank.beta.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "operational_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationalDay {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "business_date", nullable = false, unique = true)
    private LocalDate businessDate;  // Дата операционного дня

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayStatus status;  // OPENED, CLOSED

    @Column(name = "opened_at")
    private LocalDateTime openedAt;  // Когда открыли день

    @Column(name = "closed_at")
    private LocalDateTime closedAt;  // Когда закрыли день

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
    }
}