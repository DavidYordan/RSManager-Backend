package com.rsmanager.repository.remoteB;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokUserDetailsRemote;

@Repository
public interface RemoteBTiktokAccountRepository extends JpaRepository<TiktokUserDetailsRemote, String> {
    
    List<TiktokUserDetailsRemote> findByUpdatedAtAfterAndUpdatedAtIsNotNull(Instant updatedAt, PageRequest pageRequest);
}