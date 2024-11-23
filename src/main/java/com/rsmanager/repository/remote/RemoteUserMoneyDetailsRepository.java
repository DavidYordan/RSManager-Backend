package com.rsmanager.repository.remote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserMoneyDetails;

import java.util.List;

/**
 * 远程数据库的 UserMoneyDetails Repository
 */
@Repository
public interface RemoteUserMoneyDetailsRepository extends JpaRepository<UserMoneyDetails, Integer> {

    /**
     * 查询远程数据库中所有 id 大于指定值的记录
     *
     * @param id 指定的最小 id
     * @return 符合条件的 UserMoneyDetails 列表
     */
    List<UserMoneyDetails> findByIdGreaterThan(Integer id);
}
