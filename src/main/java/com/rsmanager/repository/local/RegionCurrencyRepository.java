package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.RegionCurrency;

@Repository
public interface RegionCurrencyRepository extends JpaRepository<RegionCurrency, String>, JpaSpecificationExecutor<RegionCurrency> {
    
}
