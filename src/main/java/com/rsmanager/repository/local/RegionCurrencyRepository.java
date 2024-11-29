package com.rsmanager.repository.local;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.RegionCurrency;

@Repository
public interface RegionCurrencyRepository extends JpaRepository<RegionCurrency, String>, JpaSpecificationExecutor<RegionCurrency> {
    
    @Query("select currencyCode from RegionCurrency where currencyName = ?1")
    Optional<String> findCurrencyCodeByCurrencyName(String currencyName);
}
