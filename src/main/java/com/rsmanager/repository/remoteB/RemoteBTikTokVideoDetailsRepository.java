package com.rsmanager.repository.remoteB;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokVideoDetailsRemote;

@Repository
public interface RemoteBTikTokVideoDetailsRepository extends JpaRepository<TiktokVideoDetailsRemote, Long> {
    
    List<TiktokVideoDetailsRemote> findByUpdatedAtAfterAndUpdatedAtIsNotNull(Instant updatedAt, PageRequest pageRequest);
}
