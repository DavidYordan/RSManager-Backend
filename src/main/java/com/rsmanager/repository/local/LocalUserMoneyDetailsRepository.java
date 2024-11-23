package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserMoneyDetails;

/**
 * 本地数据库的 UserMoneyDetails Repository
 */
@Repository
public interface LocalUserMoneyDetailsRepository extends JpaRepository<UserMoneyDetails, Integer> {

    /**
     * 查询本地数据库中最大的 id
     *
     * @return 最大的 id
     */
    @Query("SELECT MAX(u.id) FROM UserMoneyDetails u")
    Integer findMaxId();
}