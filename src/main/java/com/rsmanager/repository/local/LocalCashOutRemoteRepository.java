package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.CashOutRemote;

/**
 * 本地数据库的 CashOut Repository
 */
@Repository
public interface LocalCashOutRemoteRepository extends JpaRepository<CashOutRemote, Long>, LocalCashOutRepositoryCustom {

    /**
     * 查询本地数据库中最新的 createAt
     *
     * @return 最新的 createAt
     */
    @Query("SELECT MAX(c.createAt) FROM CashOutRemote c")
    String findMaxCreateAt();

    /**
     * 查询本地数据库中最新的 outAt
     *
     * @return 最新的 outAt
     */
    @Query("SELECT MAX(c.outAt) FROM CashOutRemote c")
    String findMaxOutAt();
}
