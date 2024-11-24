package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TbUserRemote;

@Repository
public interface LocalTbUserRemoteRepository extends JpaRepository<TbUserRemote, Long> {

    @Query("SELECT MAX(u.updateTime) FROM TbUserRemote u")
    String findMaxUpdateTime();
}
