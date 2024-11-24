package com.rsmanager.repository.remote;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.CashOutRemote;

import java.util.List;

/**
 * 远程数据库的 CashOut Repository
 */
@Repository
public interface RemoteCashOutRepository extends JpaRepository<CashOutRemote, Long> {

    /**
     * 查询远程数据库中所有 createAt 在指定时间之后的记录
     *
     * @param createAt 指定的时间
     * @return 符合条件的 CashOut 列表
     */
    List<CashOutRemote> findByCreateAtAfter(String createAt, PageRequest pageRequest);

    /**
     * 查询远程数据库中所有 outAt 在指定时间之后的记录
     *
     * @param outAt 指定的时间
     * @return 符合条件的 CashOut 列表
     */
    List<CashOutRemote> findByOutAtAfter(String outAt, PageRequest pageRequest);
}
