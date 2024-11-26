package com.rsmanager.repository.local;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rsmanager.dto.tbuser.InviteDailyMoneySumDTO;
import com.rsmanager.model.Invite;

/**
 * 本地数据库的 Invite Repository
 */
@Repository
public interface LocalInviteRepository extends JpaRepository<Invite, Integer> {

    /**
     * 查询本地数据库中最大的 id
     */
    @Query("SELECT MAX(i.id) FROM Invite i")
    Integer findMaxId();

    /**
     * 查询每日邀请奖励总额
     */
    @Query("SELECT com.rsmanager.dto.tbuser.InviteDailyMoneySumDTO(DATE(i.createTime), SUM(i.money)) " +
       "FROM Invite i " +
       "WHERE i.state = 1 AND i.userId = :userId " +
       "GROUP BY DATE(i.createTime)")
    List<InviteDailyMoneySumDTO> findDailyMoneySumByUserId(@Param("userId") Long userId);
}
