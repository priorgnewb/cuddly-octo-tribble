package com.bank.beta.service;

import com.bank.beta.dto.ClientDto;
import com.bank.beta.dto.ClientLoansDto;
import com.bank.beta.dto.LoanSummaryDto;
import com.bank.beta.entity.Client;
import com.bank.beta.entity.Loan;
import com.bank.beta.entity.LoanStatus;
import com.bank.beta.entity.PaymentStatus;
import com.bank.beta.repository.ClientRepository;
import com.bank.beta.repository.LoanRepository;
import com.bank.beta.repository.PaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;

    @Transactional
    public ClientDto createClient(ClientDto clientDto) {
        log.info("Creating new client: {}", clientDto.getFullName());

        Client client = new Client();
        client.setFullName(clientDto.getFullName());
        client.setPassportNumber(clientDto.getPassportNumber());
        client.setPhoneNumber(clientDto.getPhoneNumber());
        client.setEmail(clientDto.getEmail());

        Client savedClient = clientRepository.save(client);
        log.info("Client created with id: {}", savedClient.getId());

        return mapToDto(savedClient);
    }

    @Transactional(readOnly = true)
    public ClientDto getClient(UUID clientId) {
        log.info("Getting client by id: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));

        return mapToDto(client);
    }

    @Transactional(readOnly = true)
    public ClientLoansDto getClientWithLoans(UUID clientId) {
        log.info("Getting client with loans: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + clientId));

        List<Loan> clientLoans = loanRepository.findByClientId(clientId);

        List<LoanSummaryDto> activeLoans = clientLoans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE)
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        List<LoanSummaryDto> closedLoans = clientLoans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.PAID)
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        BigDecimal totalDebt = activeLoans.stream()
                .map(LoanSummaryDto::getRemainingDebt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ClientLoansDto.builder()
                .clientId(client.getId())
                .clientFullName(client.getFullName())
                .activeLoans(activeLoans)
                .closedLoans(closedLoans)
                .totalDebt(totalDebt)
                .activeLoansCount(activeLoans.size())
                .closedLoansCount(closedLoans.size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClientDto> getAllClients() {
        log.info("Getting all clients");

        return clientRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientDto> searchClients(String query) {
        log.info("Searching clients with query: {}", query);

        // Простой поиск по имени или паспорту
        return clientRepository.findAll().stream()
                .filter(client ->
                        client.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                                client.getPassportNumber().contains(query))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ClientDto mapToDto(Client client) {
        List<Loan> clientLoans = loanRepository.findByClientId(client.getId());
        long activeLoans = clientLoans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACTIVE || loan.getStatus() == LoanStatus.OVERDUE)
                .count();

        return ClientDto.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .passportNumber(client.getPassportNumber())
                .phoneNumber(client.getPhoneNumber())
                .email(client.getEmail())
                .createdAt(client.getCreatedAt())
                .activeLoansCount((int) activeLoans)
                .totalLoansCount(clientLoans.size())
                .build();
    }

    private LoanSummaryDto mapToSummaryDto(Loan loan) {
        int totalPayments = loan.getTermMonths();
        int paymentsMade = paymentScheduleRepository
                .findByLoanIdAndStatus(loan.getId(), PaymentStatus.PAID)
                .size();

        boolean hasOverdue = !paymentScheduleRepository
                .findByLoanIdAndStatus(loan.getId(), PaymentStatus.OVERDUE)
                .isEmpty();

        return LoanSummaryDto.builder()
                .id(loan.getId())
                .amount(loan.getAmount())
                .remainingDebt(loan.getRemainingDebt())
                .monthlyPayment(loan.getMonthlyPayment())
                .status(loan.getStatus().name())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .paymentsMade(paymentsMade)
                .totalPayments(totalPayments)
                .hasOverdue(hasOverdue)
                .build();
    }
}