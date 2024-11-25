package com.rsmanager.repository.local;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TbUser;

@Repository
public interface LocalTbUserRepository extends JpaRepository<TbUser, Long> {

    // 查询本地数据库中最新的 updateTime
    @Query("SELECT MAX(u.updateTime) FROM TbUser u")
    String findMaxUpdateTime();

    Optional<TbUser> findByPhone(String phone);

    List<TbUser> findByUserIdIn(List<Long> userIds);

    Optional<TbUser> findByUserId(Long userId);

    Long countByInviterCode(String inviterCode);

    List<TbUser> findByInviterCode(String inviterCode);

    Optional<TbUser> findByInvitationCode(String invitationCode);

    @Query("SELECT u.userId FROM TbUser u WHERE u.inviterCode = :inviterCode")
    List<Long> findUserIdByInviterCode(String inviterCode);

    @Query("SELECT u.invitationCode FROM TbUser u WHERE u.userId = :userId")
    String findInvitationCodeByUserId(Long userId);

    @Query("SELECT u.userId FROM TbUser u WHERE u.invitationCode = :invitationCode")
    Long findUserIdByInvitationCode(String invitationCode);

}
