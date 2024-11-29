package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.PaymentAccount;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long>, JpaSpecificationExecutor<PaymentAccount> {
    
}
