package com.rsmanager.repository.remote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.AgentWidthdraw;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RemoteAgentWidthdrawRepository extends JpaRepository<AgentWidthdraw, Long> {

    // 查询远程数据库中所有在指定时间之后更新的记录
    List<AgentWidthdraw> findByUpdateTimeAfter(LocalDateTime updateTime);
}
