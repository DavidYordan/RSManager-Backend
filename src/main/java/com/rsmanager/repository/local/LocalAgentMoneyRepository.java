package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.AgentMoney;

/**
 * 本地数据库的 AgentMoney Repository
 */
@Repository
public interface LocalAgentMoneyRepository extends JpaRepository<AgentMoney, Long> {

    /**
     * 查询本地数据库中最大的 id
     *
     * @return 最大的 id
     */
    @Query("SELECT MAX(a.id) FROM AgentMoney a")
    Long findMaxId();
}
