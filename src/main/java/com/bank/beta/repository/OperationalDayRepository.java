package com.bank.beta.repository;

import com.bank.beta.entity.DayStatus;
import com.bank.beta.entity.OperationalDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationalDayRepository extends JpaRepository<OperationalDay, UUID> {

    Optional<OperationalDay> findByBusinessDate(LocalDate date);

    @Query("SELECT od FROM OperationalDay od WHERE od.businessDate = :date AND od.status = :status")
    Optional<OperationalDay> findByDateAndStatus(@Param("date") LocalDate date, @Param("status") DayStatus status);

    @Query("SELECT CASE WHEN COUNT(od) > 0 THEN true ELSE false END FROM OperationalDay od WHERE od.businessDate = :date AND od.status = 'OPENED'")
    boolean isDayOpened(@Param("date") LocalDate date);

    @Query("SELECT od FROM OperationalDay od WHERE od.status = 'OPENED' ORDER BY od.businessDate DESC")
    Optional<OperationalDay> findCurrentOpenedDay();
}