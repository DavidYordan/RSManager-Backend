package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserIntegralRemote;

@Repository
public interface LocalUserIntegralRemoteRepository extends JpaRepository<UserIntegralRemote, Integer> {
    // 标准的 CRUD 操作由 JpaRepository 提供
}
