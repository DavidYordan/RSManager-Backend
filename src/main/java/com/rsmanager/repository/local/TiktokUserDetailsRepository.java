package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.rsmanager.model.TiktokUserDetails;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TiktokUserDetailsRepository extends JpaRepository<TiktokUserDetails, String>, JpaSpecificationExecutor<TiktokUserDetails> {
    
    // 根据tiktok_account查找
    Optional<TiktokUserDetails> findByTiktokAccount(String tiktokAccount);

    @Query("SELECT MAX(t.updatedAt) FROM TiktokUserDetails t")
    Instant findMaxUpdateAt();
}