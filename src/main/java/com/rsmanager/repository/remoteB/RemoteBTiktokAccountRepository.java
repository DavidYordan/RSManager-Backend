package com.rsmanager.repository.remoteB;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokAccount;

@Repository
public interface RemoteBTiktokAccountRepository extends JpaRepository<TiktokAccount, String> {
    
    List<TiktokAccount> findByUpdatedAtAfterAndUpdatedAtIsNotNull(LocalDateTime updatedAt);
}