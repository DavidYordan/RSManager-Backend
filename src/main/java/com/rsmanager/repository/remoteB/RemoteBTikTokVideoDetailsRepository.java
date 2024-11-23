package com.rsmanager.repository.remoteB;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokVideoDetails;

@Repository
public interface RemoteBTikTokVideoDetailsRepository extends JpaRepository<TiktokVideoDetails, Long> {
    
    List<TiktokVideoDetails> findByUpdatedAtAfterAndUpdatedAtIsNotNull(LocalDateTime updatedAt);
}
