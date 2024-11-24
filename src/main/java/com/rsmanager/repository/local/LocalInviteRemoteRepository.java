package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.InviteRemote;

/**
 * 本地数据库的 Invite Repository
 */
@Repository
public interface LocalInviteRemoteRepository extends JpaRepository<InviteRemote, Integer> {

    /**
     * 查询本地数据库中最大的 id
     *
     * @return 最大的 id
     */
    @Query("SELECT MAX(i.id) FROM InviteRemote i")
    Integer findMaxId();
}
