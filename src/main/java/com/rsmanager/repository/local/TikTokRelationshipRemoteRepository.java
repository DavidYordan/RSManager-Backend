package com.rsmanager.repository.local;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokRelationshipRemote;

@Repository
public interface TikTokRelationshipRemoteRepository extends JpaRepository<TiktokRelationshipRemote, Long> {

    List<TiktokRelationshipRemote> findByTiktokAccountIn(List<String> tiktokAccounts);
}
