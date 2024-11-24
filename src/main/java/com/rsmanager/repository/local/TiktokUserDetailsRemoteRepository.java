package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.rsmanager.model.TiktokUserDetailsRemote;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TiktokUserDetailsRemoteRepository extends JpaRepository<TiktokUserDetailsRemote, String>, JpaSpecificationExecutor<TiktokUserDetailsRemote> {
    
    // 根据tiktok_account查找
    Optional<TiktokUserDetailsRemote> findByTiktokAccount(String tiktokAccount);

    @Query("SELECT MAX(t.updatedAt) FROM TiktokUserDetailsRemote t")
    Instant findMaxUpdateAt();
}