package com.rsmanager.repository.local;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokVideoDetailsRemote;

@Repository
public interface TikTokVideoDetailsRemoteRepository extends JpaRepository<TiktokVideoDetailsRemote, Long> {
    
    @Query("SELECT MAX(t.updatedAt) FROM TiktokVideoDetailsRemote t")
    Instant findMaxUpdateAt();
}
