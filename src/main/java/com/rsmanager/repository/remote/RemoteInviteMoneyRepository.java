package com.rsmanager.repository.remote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.InviteMoney;

import java.util.List;

@Repository
public interface RemoteInviteMoneyRepository extends JpaRepository<InviteMoney, Long> {
    
    /**
     * 根据指定的 userIds 批量查询 InviteMoney 记录
     * @param userIds 用户ID列表
     * @return 满足条件的 InviteMoney 列表
     */
    List<InviteMoney> findByUserIdIn(List<Integer> userIds);
}
