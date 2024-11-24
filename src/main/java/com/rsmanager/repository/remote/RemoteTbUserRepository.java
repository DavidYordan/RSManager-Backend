package com.rsmanager.repository.remote;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TbUserRemote;

import java.util.List;

@Repository
public interface RemoteTbUserRepository extends JpaRepository<TbUserRemote, Long> {

    // 查询远程数据库中所有在指定时间之后更新的用户
    List<TbUserRemote> findByUpdateTimeAfter(String updateTime, PageRequest pageRequest);
}
