package com.rsmanager.repository.local;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.InviteMoneyRemote;

@Repository
public interface LocalInviteMoneyRemoteRepository extends JpaRepository<InviteMoneyRemote, Long> {
    Optional<InviteMoneyRemote> findByUserId(Long userId);
}
