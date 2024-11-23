package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.rsmanager.model.TiktokAccount;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TiktokAccountRepository extends JpaRepository<TiktokAccount, String>, JpaSpecificationExecutor<TiktokAccount> {
    
    // 根据tiktok_account查找
    Optional<TiktokAccount> findByTiktokAccount(String tiktokAccount);

    @Query("SELECT MAX(t.updatedAt) FROM TiktokAccount t")
    LocalDateTime findMaxUpdateAt();
}