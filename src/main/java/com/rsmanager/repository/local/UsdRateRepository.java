package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UsdRate;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsdRateRepository extends JpaRepository<UsdRate, Long> {

    @Query("SELECT MAX(ur.date) FROM UsdRate ur")
    Optional<LocalDate> findLatestDate();

    @Query("SELECT ur.rate FROM UsdRate ur WHERE ur.date = :date AND ur.currencyCode = :currencyCode")
    Optional<Double> findRateByDateAndCurrencyCode(@Param("date") LocalDate date, @Param("currencyCode") String currencyCode);

    @Query("SELECT u FROM UsdRate u WHERE u.date = :date AND u.currencyCode IN :currencyCodes")
    List<UsdRate> findByDateAndCurrencyCodeIn(@Param("date") LocalDate date, @Param("currencyCodes") Collection<String> currencyCodes);
}