package com.rsmanager.repository.remote;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.InviteRemote;

import java.util.List;

/**
 * 远程数据库的 Invite Repository
 */
@Repository
public interface RemoteInviteRepository extends JpaRepository<InviteRemote, Integer> {

    /**
     * 查询远程数据库中所有 id 大于指定值的记录
     *
     * @param id 指定的最小 id
     * @return 符合条件的 Invite 列表
     */
    List<InviteRemote> findByIdGreaterThan(Integer id, PageRequest pageRequest);
}
