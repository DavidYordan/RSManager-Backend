package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.Invite;

/**
 * 本地数据库的 Invite Repository
 */
@Repository
public interface LocalInviteRepository extends JpaRepository<Invite, Integer> {

    /**
     * 查询本地数据库中最大的 id
     *
     * @return 最大的 id
     */
    @Query("SELECT MAX(i.id) FROM Invite i")
    Integer findMaxId();
}
