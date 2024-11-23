package com.rsmanager.repository.remoteB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokRelationshipRemoteB;


@Repository
public interface RemoteBTikTokRelationshipRepository extends JpaRepository<TiktokRelationshipRemoteB, Long> {
    
}
