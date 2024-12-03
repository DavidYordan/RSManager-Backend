package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.rsmanager.model.TiktokUserDetails;

import java.time.Instant;

@Repository
public interface TiktokUserDetailsRepository extends JpaRepository<TiktokUserDetails, String>, JpaSpecificationExecutor<TiktokUserDetails> {

    @Query("SELECT MAX(t.updatedAt) FROM TiktokUserDetails t")
    Instant findMaxUpdateAt();
}