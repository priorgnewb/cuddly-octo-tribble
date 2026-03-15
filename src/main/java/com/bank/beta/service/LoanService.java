package com.bank.beta.service;

import com.bank.beta.dto.LoanRequestDto;
import com.bank.beta.dto.LoanResponseDto;
import com.bank.beta.dto.PaymentRequestDto;
import com.bank.beta.entity.Client;
import com.bank.beta.entity.Loan;
import com.bank.beta.entity.LoanStatus;
import com.bank.beta.entity.Payment;
import com.bank.beta.entity.PaymentSchedule;
import com.bank.beta.entity.PaymentStatus;
import com.bank.beta.entity.PaymentType;
import com.bank.beta.repository.ClientRepository;
import com.bank.beta.repository.LoanRepository;
import com.bank.beta.repository.PaymentRepository;
import com.bank.beta.repository.PaymentScheduleRepository;
import com.bank.beta.util.LoanCalculator;
import com.bank.beta.util.ClientGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.beta.dto.*;
import com.bank.beta.entity.PaymentStatus;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final ClientRepository clientRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;  // Вот это было нужно!
    private final LoanCalculator loanCalculator;
    private final ClientGenerator clientGenerator;

    @Transactional
    public LoanResponseDto createLoan(LoanRequestDto request) {
        log.info("Creating loan for client: {}", request.getClientId());

        // Находим или создаем клиента
        Client client = getOrCreateClient(request);

        // Рассчитываем ежемесячный платеж
        BigDecimal monthlyPayment = loanCalculator.calculateMonthlyPayment(
                request.getAmount(),
                request.getInterestRate(),
                request.getTermMonths()
        );

        // Создаем кредит
        Loan loan = new Loan();
        loan.setClient(client);
        loan.setAmount(request.getAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setTermMonths(request.getTermMonths());
        loan.setStartDate(request.getStartDate());
        loan.setEndDate(loanCalculator.calculateEndDate(request.getStartDate(), request.getTermMonths()));
        loan.setMonthlyPayment(monthlyPayment);
        loan.setRemainingDebt(request.getAmount());
        loan.setStatus(LoanStatus.ACTIVE);

        // Сохраняем кредит
        Loan savedLoan = loanRepository.save(loan);

        // Генерируем график платежей
        generatePaymentSchedule(savedLoan);

        log.info("Loan created successfully with id: {}", savedLoan.getId());

        // Формируем ответ
        return mapToResponse(savedLoan);
    }

    @Transactional
    public void generatePaymentSchedule(Loan loan) {
        log.info("Generating payment schedule for loan: {}", loan.getId());

        List<PaymentSchedule> schedule = new ArrayList<>();
        LocalDate currentDate = loan.getStartDate();

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            PaymentSchedule payment = new PaymentSchedule();
            payment.setLoan(loan);
            payment.setPaymentNumber(i);
            payment.setDueDate(currentDate.plusMonths(i));
            payment.setPlannedAmount(loan.getMonthlyPayment());
            payment.setStatus(PaymentStatus.PENDING);

            // Расчет процентов и основного долга для информативности
            BigDecimal[] parts = calculatePaymentParts(loan, i);
            payment.setPrincipalPart(parts[0]);
            payment.setInterestPart(parts[1]);

            schedule.add(payment);
        }

        paymentScheduleRepository.saveAll(schedule);
        log.info("Generated {} payments for loan", schedule.size());
    }

    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        log.info("Processing payment for loan: {}, amount: {}", request.getLoanId(), request.getAmount());

        Loan loan = loanRepository.findById(UUID.fromString(request.getLoanId()))
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + request.getLoanId()));

        // Проверяем, активен ли кредит
        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new RuntimeException("Loan is not active. Current status: " + loan.getStatus());
        }

        // Находим ближайший ожидаемый платеж
        List<PaymentSchedule> pendingPayments = paymentScheduleRepository
                .findPendingPayments(loan.getId());

        if (pendingPayments.isEmpty()) {
            throw new RuntimeException("No pending payments found for loan: " + loan.getId());
        }

        PaymentSchedule currentPayment = pendingPayments.get(0);
        BigDecimal paymentAmount = request.getAmount();

        // Рассчитываем распределение платежа
        BigDecimal[] parts = calculatePaymentDistribution(loan, currentPayment, paymentAmount);
        BigDecimal principalPart = parts[0];
        BigDecimal interestPart = parts[1];

        // Создаем запись о платеже
        Payment payment = new Payment();
        payment.setLoan(loan);
        payment.setPaymentSchedule(currentPayment);
        payment.setAmount(paymentAmount);
        payment.setPaymentDate(request.getPaymentDate());
        payment.setType(request.isEarlyPayment() ? PaymentType.EARLY : PaymentType.SCHEDULED);
        payment.setPrincipalPart(principalPart);
        payment.setInterestPart(interestPart);

        // Сохраняем платеж
        Payment savedPayment = paymentRepository.save(payment);

        // Обновляем график платежей
        updatePaymentSchedule(currentPayment, paymentAmount, request.getPaymentDate());

        // Обновляем остаток долга по кредиту
        updateLoanRemainingDebt(loan, principalPart);

        // Сохраняем изменения кредита
        loanRepository.save(loan);

        log.info("Payment processed successfully. Payment id: {}", savedPayment.getId());

        // Возвращаем DTO, а не Entity
        return mapToPaymentResponse(savedPayment, loan);
    }
    private BigDecimal[] calculatePaymentParts(Loan loan, int paymentNumber) {
        // Упрощенный расчет для демонстрации
        // В реальном проекте тут должен быть полноценный расчет с аннуитетной формулой
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal remainingDebt = loan.getAmount()
                .multiply(BigDecimal.valueOf(loan.getTermMonths() - paymentNumber + 1))
                .divide(BigDecimal.valueOf(loan.getTermMonths()), 2, RoundingMode.HALF_UP);

        BigDecimal interestPart = remainingDebt.multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal principalPart = loan.getMonthlyPayment().subtract(interestPart)
                .setScale(2, RoundingMode.HALF_UP);

        return new BigDecimal[]{principalPart, interestPart};
    }

    private BigDecimal[] calculatePaymentDistribution(Loan loan, PaymentSchedule schedule, BigDecimal paymentAmount) {
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal interestPart = loan.getRemainingDebt()
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal principalPart = paymentAmount.min(loan.getRemainingDebt());

        return new BigDecimal[]{principalPart, interestPart};
    }

    private void updatePaymentSchedule(PaymentSchedule schedule, BigDecimal paidAmount, LocalDate paidDate) {
        if (paidAmount.compareTo(schedule.getPlannedAmount()) >= 0) {
            schedule.setStatus(PaymentStatus.PAID);
            schedule.setActualAmount(paidAmount);
            schedule.setPaidDate(paidDate);
        } else {
            schedule.setStatus(PaymentStatus.PARTIALLY_PAID);
            schedule.setActualAmount(paidAmount);
        }
        paymentScheduleRepository.save(schedule);
    }

    private void updateLoanRemainingDebt(Loan loan, BigDecimal paidPrincipal) {
        BigDecimal newRemainingDebt = loan.getRemainingDebt()
                .subtract(paidPrincipal)
                .max(BigDecimal.ZERO);
        loan.setRemainingDebt(newRemainingDebt);

        if (newRemainingDebt.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.PAID);
            log.info("Loan {} fully paid", loan.getId());
        }
    }

    private Client getOrCreateClient(LoanRequestDto request) {
        // Если передан clientId, пробуем найти
        if (request.getClientId() != null && !request.getClientId().isEmpty()) {
            try {
                UUID clientUuid = UUID.fromString(request.getClientId());
                return clientRepository.findById(clientUuid)
                        .orElseGet(() -> {
                            log.info("Client with id {} not found, creating new client", request.getClientId());
                            return createClientFromRequest(request);
                        });
            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format: {}, creating new client", request.getClientId());
                return createClientFromRequest(request);
            }
        }

        // Если clientId не передан, создаем нового клиента
        log.info("No client ID provided, creating new client");
        return createClientFromRequest(request);
    }

    private Client createClientFromRequest(LoanRequestDto request) {
        Client client;

        // Если в запросе есть данные клиента, используем их
        if (request.getFullName() != null || request.getPassportNumber() != null) {
            client = new Client();
            client.setFullName(request.getFullName() != null ? request.getFullName() : "Unknown");
            client.setPassportNumber(request.getPassportNumber() != null ? request.getPassportNumber() : generatePassport());
            client.setPhoneNumber(request.getPhoneNumber());
            client.setEmail(request.getEmail());
            log.info("Creating client with provided data: {}", client.getFullName());
        } else {
            // Иначе генерируем читаемого клиента
            client = clientGenerator.generateReadableClient();
            log.info("Generating readable client: {}", client.getFullName());
        }

        return clientRepository.save(client);
    }

    private String generatePassport() {
        return "AB" + (1000000 + (int)(Math.random() * 9000000));
    }

    private LoanResponseDto mapToResponse(Loan loan) {
        return LoanResponseDto.builder()
                .id(loan.getId())
                .clientId(loan.getClient().getId())
                .clientFullName(loan.getClient().getFullName())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .monthlyPayment(loan.getMonthlyPayment())
                .remainingDebt(loan.getRemainingDebt())
                .status(loan.getStatus().name())
                .createdAt(loan.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public LoanDetailDto getLoanDetails(UUID loanId) {
        log.info("Getting loan details for id: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        List<PaymentSchedule> schedule = paymentScheduleRepository
                .findByLoanIdOrderByPaymentNumberAsc(loanId);

        List<LoanDetailDto.ScheduleItemDto> scheduleItems = schedule.stream()
                .map(this::mapToScheduleItemDto)
                .collect(Collectors.toList());

        return LoanDetailDto.builder()
                .loanId(loan.getId())
                .totalAmount(loan.getAmount())
                .remainingDebt(loan.getRemainingDebt())
                .monthlyPayment(loan.getMonthlyPayment())
                .status(loan.getStatus().name())
                .paymentSchedule(scheduleItems)
                .build();
    }

    @Transactional(readOnly = true)
    public List<LoanSummaryDto> getClientLoans(UUID clientId) {
        log.info("Getting loans for client: {}", clientId);

        List<Loan> loans = loanRepository.findByClientId(clientId);

        return loans.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentScheduleDto getPaymentSchedule(UUID loanId) {
        log.info("Getting payment schedule for loan: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        List<PaymentSchedule> schedule = paymentScheduleRepository
                .findByLoanIdOrderByPaymentNumberAsc(loanId);

        BigDecimal paidAmount = schedule.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .map(PaymentSchedule::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PaymentScheduleDto.SchedulePaymentDto> paymentDtos = schedule.stream()
                .map(this::mapToSchedulePaymentDto)
                .collect(Collectors.toList());

        return PaymentScheduleDto.builder()
                .loanId(loanId)
                .totalAmount(loan.getAmount())
                .paidAmount(paidAmount)
                .remainingAmount(loan.getRemainingDebt())
                .totalPayments(schedule.size())
                .paidPayments((int) schedule.stream().filter(p -> p.getStatus() == PaymentStatus.PAID).count())
                .payments(paymentDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public RemainingDebtDto getRemainingDebt(UUID loanId) {
        log.info("Getting remaining debt for loan: {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        List<PaymentSchedule> pendingPayments = paymentScheduleRepository
                .findPendingPayments(loanId);

        BigDecimal nextPaymentAmount = pendingPayments.isEmpty() ?
                BigDecimal.ZERO : pendingPayments.get(0).getPlannedAmount();

        LocalDate nextPaymentDate = pendingPayments.isEmpty() ?
                null : pendingPayments.get(0).getDueDate();

        List<PaymentSchedule> overduePayments = paymentScheduleRepository
                .findByLoanIdAndStatus(loanId, PaymentStatus.OVERDUE);

        BigDecimal overdueAmount = overduePayments.stream()
                .map(PaymentSchedule::getPlannedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RemainingDebtDto.builder()
                .loanId(loanId)
                .remainingDebt(loan.getRemainingDebt())
                .totalAmount(loan.getAmount())
                .paidAmount(loan.getAmount().subtract(loan.getRemainingDebt()))
                .nextPaymentAmount(nextPaymentAmount)
                .nextPaymentDate(nextPaymentDate)
                .remainingPayments(pendingPayments.size())
                .status(loan.getStatus().name())
                .isOverdue(loan.getStatus() == LoanStatus.OVERDUE)
                .overdueAmount(overdueAmount)
                .build();
    }

    @Transactional(readOnly = true)
    public EarlyPaymentCalculationDto calculateEarlyPayment(UUID loanId, BigDecimal amount, LocalDate paymentDate) {
        log.info("Calculating early payment for loan: {}, amount: {}", loanId, amount);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        // Расчет досрочного погашения
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // Проценты за текущий период
        BigDecimal interestForPeriod = loan.getRemainingDebt()
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        // Остаток после оплаты процентов
        BigDecimal remainingForPrincipal = amount.subtract(interestForPeriod)
                .max(BigDecimal.ZERO);

        // Новый остаток долга
        BigDecimal newRemainingDebt = loan.getRemainingDebt()
                .subtract(remainingForPrincipal)
                .max(BigDecimal.ZERO);

        // Расчет экономии
        BigDecimal totalInterestSaved = calculateInterestSaved(loan, remainingForPrincipal);

        return EarlyPaymentCalculationDto.builder()
                .loanId(loanId)
                .paymentAmount(amount)
                .paymentDate(paymentDate)
                .principalPaid(remainingForPrincipal)
                .interestPaid(interestForPeriod)
                .newRemainingDebt(newRemainingDebt)
                .monthsReduced(estimateMonthsReduced(loan, remainingForPrincipal))
                .totalInterestSaved(totalInterestSaved)
                .build();
    }

    @Transactional
    public PaymentResponseDto processEarlyPayment(EarlyPaymentRequestDto request) {
        log.info("Processing early payment for loan: {}", request.getLoanId());

        // Создаем обычный платеж
        PaymentRequestDto paymentRequest = new PaymentRequestDto(
                request.getLoanId().toString(),
                request.getAmount(),
                request.getPaymentDate(),
                true
        );

        // processPayment теперь возвращает PaymentResponseDto, а не Payment
        PaymentResponseDto paymentResponse = processPayment(paymentRequest);

        // Затем пересчитываем график платежей
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (request.isReduceTerm()) {
            recalculateScheduleReduceTerm(loan);
        } else {
            recalculateScheduleReducePayment(loan);
        }

        // Возвращаем PaymentResponseDto с обновленным остатком
        paymentResponse.setRemainingDebtAfter(loan.getRemainingDebt());

        return paymentResponse;
    }
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getLoanPayments(UUID loanId) {
        log.info("Getting payments for loan: {}", loanId);

        List<Payment> payments = paymentRepository.findByLoanId(loanId);

        return payments.stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentHistoryDto getPaymentHistory(UUID clientId, LocalDate fromDate, LocalDate toDate, int page, int size) {
        log.info("Getting payment history for client: {}", clientId);

        // В реальном проекте тут должен быть запрос с пагинацией
        // Для простоты возвращаем все платежи
        List<Payment> allPayments = paymentRepository.findAll();

        List<PaymentResponseDto> payments = allPayments.stream()
                .filter(p -> clientId == null || p.getLoan().getClient().getId().equals(clientId))
                .filter(p -> fromDate == null || !p.getPaymentDate().isBefore(fromDate))
                .filter(p -> toDate == null || !p.getPaymentDate().isAfter(toDate))
                .skip(page * size)
                .limit(size)
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = payments.stream()
                .map(PaymentResponseDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrincipal = payments.stream()
                .map(PaymentResponseDto::getPrincipalPart)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInterest = payments.stream()
                .map(PaymentResponseDto::getInterestPart)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PaymentHistoryDto.builder()
                .payments(payments)
                .totalCount(payments.size())
                .page(page)
                .size(size)
                .totalAmount(totalAmount)
                .totalPrincipal(totalPrincipal)
                .totalInterest(totalInterest)
                .build();
    }

// Приватные вспомогательные методы

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

    private LoanDetailDto.ScheduleItemDto mapToScheduleItemDto(PaymentSchedule payment) {
        return LoanDetailDto.ScheduleItemDto.builder()
                .paymentNumber(payment.getPaymentNumber())
                .dueDate(payment.getDueDate())
                .plannedAmount(payment.getPlannedAmount())
                .actualAmount(payment.getActualAmount())
                .status(payment.getStatus().name())
                .paidDate(payment.getPaidDate())
                .build();
    }

    private PaymentScheduleDto.SchedulePaymentDto mapToSchedulePaymentDto(PaymentSchedule payment) {
        boolean isOverdue = payment.getStatus() == PaymentStatus.OVERDUE;
        int daysOverdue = isOverdue ?
                calculateDaysOverdue(payment.getDueDate(), LocalDate.now()) : 0;

        return PaymentScheduleDto.SchedulePaymentDto.builder()
                .number(payment.getPaymentNumber())
                .dueDate(payment.getDueDate())
                .plannedAmount(payment.getPlannedAmount())
                .actualAmount(payment.getActualAmount())
                .status(payment.getStatus().name())
                .principalPart(payment.getPrincipalPart())
                .interestPart(payment.getInterestPart())
                .paidDate(payment.getPaidDate())
                .isOverdue(isOverdue)
                .daysOverdue(daysOverdue)
                .build();
    }

    private PaymentResponseDto mapToPaymentResponse(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .loanId(payment.getLoan().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .type(payment.getType().name())
                .principalPart(payment.getPrincipalPart())
                .interestPart(payment.getInterestPart())
                .paymentNumber(payment.getPaymentSchedule() != null ?
                        payment.getPaymentSchedule().getPaymentNumber() : null)
                .createdAt(payment.getCreatedAt())
                .remainingDebtAfter(payment.getLoan().getRemainingDebt())
                .status(payment.getPaymentSchedule() != null ?
                        payment.getPaymentSchedule().getStatus().name() : "PROCESSED")
                .build();
    }

    private PaymentResponseDto mapToPaymentResponse(Payment payment, Loan loan) {
        PaymentResponseDto dto = mapToPaymentResponse(payment);
        dto.setRemainingDebtAfter(loan.getRemainingDebt());
        return dto;
    }

    private BigDecimal calculateInterestSaved(Loan loan, BigDecimal earlyPaymentAmount) {
        // Упрощенный расчет экономии на процентах
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        return earlyPaymentAmount
                .multiply(monthlyRate)
                .multiply(new BigDecimal(loan.getTermMonths() / 2))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private int estimateMonthsReduced(Loan loan, BigDecimal earlyPaymentAmount) {
        // Примерная оценка сокращения срока
        return earlyPaymentAmount
                .divide(loan.getMonthlyPayment(), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private void recalculateScheduleReduceTerm(Loan loan) {
        // Уменьшаем срок кредита
        // TODO: реализовать пересчет графика с уменьшением срока
        log.info("Recalculating schedule with reduced term for loan: {}", loan.getId());
    }

    private void recalculateScheduleReducePayment(Loan loan) {
        // Уменьшаем ежемесячный платеж
        // TODO: реализовать пересчет графика с уменьшением платежа
        log.info("Recalculating schedule with reduced payment for loan: {}", loan.getId());
    }

    private int calculateDaysOverdue(LocalDate dueDate, LocalDate currentDate) {
        return (int) dueDate.until(currentDate).getDays();
    }

}

