package com.rsmanager.repository.remoteB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokRelationshipRemote;

@Repository
public interface RemoteBTikTokRelationshipRepository extends JpaRepository<TiktokRelationshipRemote, Long> {
    
}
