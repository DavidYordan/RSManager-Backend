package com.rsmanager.repository.local;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserMoney;

@Repository
public interface LocalUserMoneyRepository extends JpaRepository<UserMoney, Integer> {
    // findByUserId
    Optional<UserMoney> findByUserId(Integer userId);
}
