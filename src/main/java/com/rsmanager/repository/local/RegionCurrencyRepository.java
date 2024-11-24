package com.rsmanager.repository.local;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.RegionCurrency;

@Repository
public interface RegionCurrencyRepository extends JpaRepository<RegionCurrency, Integer>, JpaSpecificationExecutor<RegionCurrency> {
    
    @Query("SELECT rc.currencyCode FROM RegionCurrency rc")
    List<String> findAllCurrencyCodes();
}
