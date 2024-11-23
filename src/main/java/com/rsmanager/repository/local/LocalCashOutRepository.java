package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.CashOut;

/**
 * 本地数据库的 CashOut Repository
 */
@Repository
public interface LocalCashOutRepository extends JpaRepository<CashOut, Long>, LocalCashOutRepositoryCustom {

    /**
     * 查询本地数据库中最新的 createAt
     *
     * @return 最新的 createAt
     */
    @Query("SELECT MAX(c.createAt) FROM CashOut c")
    String findMaxCreateAt();

    /**
     * 查询本地数据库中最新的 outAt
     *
     * @return 最新的 outAt
     */
    @Query("SELECT MAX(c.outAt) FROM CashOut c")
    String findMaxOutAt();
}
