package com.rsmanager.repository.local;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokVideoDetails;

@Repository
public interface TikTokVideoDetailsRepository extends JpaRepository<TiktokVideoDetails, Long> {
    
    @Query("SELECT MAX(t.updatedAt) FROM TiktokVideoDetails t")
    LocalDateTime findMaxUpdateAt();
}
