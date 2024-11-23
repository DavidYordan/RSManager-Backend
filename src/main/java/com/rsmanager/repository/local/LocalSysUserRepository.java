package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.SysUser;

/**
 * 本地数据库的 SysUser Repository
 */
@Repository
public interface LocalSysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 查询本地数据库中最大的 userId
     *
     * @return 最大的 userId
     */
    @Query("SELECT MAX(a.userId) FROM SysUser a")
    Long findMaxUserId();
}
