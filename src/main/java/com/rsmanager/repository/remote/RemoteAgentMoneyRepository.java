package com.rsmanager.repository.remote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.AgentMoney;

import java.util.List;

/**
 * 远程数据库的 AgentMoney Repository
 */
@Repository
public interface RemoteAgentMoneyRepository extends JpaRepository<AgentMoney, Long> {

    /**
     * 查询远程数据库中所有 id 大于指定值的记录
     *
     * @param id 指定的最小 id
     * @return 符合条件的 AgentMoney 列表
     */
    List<AgentMoney> findByIdGreaterThan(Long id);
}
