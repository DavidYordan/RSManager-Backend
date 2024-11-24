package com.rsmanager.repository.remote;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.SysUser;

import java.util.List;

/**
 * 远程数据库的 SysUser Repository
 */
@Repository
public interface RemoteSysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 查询远程数据库中所有 userId 大于指定值的记录
     *
     * @param userId 指定的最小 userId
     * @return 符合条件的 SysUser 列表
     */
    List<SysUser> findByUserIdGreaterThan(Long userId, PageRequest pageRequest);
}
