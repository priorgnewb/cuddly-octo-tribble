package com.bank.beta.entity;

public enum PaymentStatus {
    PENDING,     // Ожидает оплаты
    PAID,        // Оплачен
    OVERDUE,     // Просрочен
    PARTIALLY_PAID // Частично оплачен
}