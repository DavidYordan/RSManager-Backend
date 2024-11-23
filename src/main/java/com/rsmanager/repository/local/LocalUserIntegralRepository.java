package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserIntegral;

@Repository
public interface LocalUserIntegralRepository extends JpaRepository<UserIntegral, Integer> {
    // 标准的 CRUD 操作由 JpaRepository 提供
}
