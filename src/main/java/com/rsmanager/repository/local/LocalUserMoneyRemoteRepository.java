package com.rsmanager.repository.local;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserMoneyRemote;

@Repository
public interface LocalUserMoneyRemoteRepository extends JpaRepository<UserMoneyRemote, Integer> {
    // findByUserId
    Optional<UserMoneyRemote> findByUserId(Integer userId);
}