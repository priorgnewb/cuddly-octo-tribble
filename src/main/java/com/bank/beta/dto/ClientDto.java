package com.bank.beta.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ClientDto {
    private UUID id;
    private String fullName;
    private String passportNumber;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
    private int activeLoansCount;
    private int totalLoansCount;
}