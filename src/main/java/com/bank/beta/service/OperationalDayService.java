package com.bank.beta.service;

import com.bank.beta.dto.BankDayCloseDto;
import com.bank.beta.entity.DayStatus;
import com.bank.beta.entity.OperationalDay;
import com.bank.beta.repository.OperationalDayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationalDayService {

    private final OperationalDayRepository operationalDayRepository;
    private final BankDayService bankDayService;

    @Transactional
    public OperationalDay openDay() {
        LocalDate today = LocalDate.now();
        return openDay(today);
    }

    @Transactional
    public OperationalDay openDay(LocalDate date) {
        log.info("Attempting to open operational day for date: {}", date);

        // Проверяем, не открыт ли уже день
        if (operationalDayRepository.isDayOpened(date)) {
            log.info("Day {} is already opened", date);
            return operationalDayRepository.findByDateAndStatus(date, DayStatus.OPENED).orElseThrow();
        }

        // Проверяем, не был ли день уже закрыт
        Optional<OperationalDay> existingDay = operationalDayRepository.findByBusinessDate(date);
        if (existingDay.isPresent() && existingDay.get().getStatus() == DayStatus.CLOSED) {
            log.info("Day {} was already closed. Cannot reopen.", date);
            throw new RuntimeException("Cannot reopen closed day: " + date);
        }

        // Создаем новый операционный день
        OperationalDay day = new OperationalDay();
        day.setBusinessDate(date);
        day.setStatus(DayStatus.OPENED);
        day.setOpenedAt(LocalDateTime.now());

        OperationalDay savedDay = operationalDayRepository.save(day);
        log.info("Operational day {} opened successfully with id: {}", date, savedDay.getId());

        return savedDay;
    }

    @Transactional
    public OperationalDay closeDay() {
        LocalDate today = LocalDate.now();
        return closeDay(today);
    }

    @Transactional
    public OperationalDay closeDay(LocalDate date) {
        log.info("Attempting to close operational day for date: {}", date);

        OperationalDay day = operationalDayRepository.findByDateAndStatus(date, DayStatus.OPENED)
                .orElseThrow(() -> new RuntimeException("No opened day found for date: " + date));

        // Выполняем все операции закрытия дня - передаем дату
        BankDayCloseDto closeResult = bankDayService.closeDay(date);

        // Закрываем день
        day.setStatus(DayStatus.CLOSED);
        day.setClosedAt(LocalDateTime.now());

        OperationalDay closedDay = operationalDayRepository.save(day);
        log.info("Operational day {} closed successfully", date);

        return closedDay;
    }

    @Transactional
    public OperationalDay ensureDayOpenedForDate(LocalDate date) {
        log.info("Ensuring operational day is opened for date: {}", date);

        // Проверяем, открыт ли день для этой даты
        if (operationalDayRepository.isDayOpened(date)) {
            log.info("Day {} is already opened", date);
            return operationalDayRepository.findByDateAndStatus(date, DayStatus.OPENED).orElseThrow();
        }

        // Проверяем, не был ли день закрыт
        Optional<OperationalDay> existingDay = operationalDayRepository.findByBusinessDate(date);
        if (existingDay.isPresent()) {
            if (existingDay.get().getStatus() == DayStatus.CLOSED) {
                log.info("Day {} was closed. Cannot reopen.", date);
                throw new RuntimeException("Cannot reopen closed day: " + date);
            }
        }

        // Создаем новый день
        OperationalDay day = new OperationalDay();
        day.setBusinessDate(date);
        day.setStatus(DayStatus.OPENED);
        day.setOpenedAt(LocalDateTime.now());

        OperationalDay savedDay = operationalDayRepository.save(day);
        log.info("Operational day {} opened successfully on startup", date);

        return savedDay;
    }

    @Transactional(readOnly = true)
    public boolean isDayOpened() {
        return isDayOpened(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public boolean isDayOpened(LocalDate date) {
        return operationalDayRepository.isDayOpened(date);
    }

    @Transactional(readOnly = true)
    public OperationalDay getCurrentDay() {
        return operationalDayRepository.findCurrentOpenedDay()
                .orElseThrow(() -> new RuntimeException("No opened day found"));
    }

    @Transactional
    public void ensureDayOpened() {
        if (!isDayOpened()) {
            openDay();
        }
    }
}