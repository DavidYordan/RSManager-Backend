package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UsdRate;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UsdRateRepository extends JpaRepository<UsdRate, Long> {

    @Query("SELECT ur.date FROM UsdRate ur ORDER BY ur.date DESC")
    Optional<LocalDate> findLatestDate();

    Optional<UsdRate> findByDateAndCurrencyCode(LocalDate date, String currencyCode);
}