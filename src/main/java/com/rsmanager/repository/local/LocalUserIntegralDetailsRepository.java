package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserIntegralDetails;

/**
 * 本地数据库的 UserIntegralDetails Repository
 */
@Repository
public interface LocalUserIntegralDetailsRepository extends JpaRepository<UserIntegralDetails, Integer> {

    /**
     * 查询本地数据库中最大的 id
     *
     * @return 最大的 id
     */
    @Query("SELECT MAX(u.id) FROM UserIntegralDetails u")
    Integer findMaxId();
}
