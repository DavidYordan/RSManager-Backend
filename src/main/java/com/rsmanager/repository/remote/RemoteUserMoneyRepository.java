package com.rsmanager.repository.remote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.UserMoney;

import java.util.List;

@Repository
public interface RemoteUserMoneyRepository extends JpaRepository<UserMoney, Long> {
    
    /**
     * 根据指定的 userIds 批量查询 UserMoney 记录
     * @param userIds 用户ID列表
     * @return 满足条件的 UserMoney 列表
     */
    List<UserMoney> findByUserIdIn(List<Integer> userIds);
}
