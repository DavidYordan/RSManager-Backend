package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokRelationship;

@Repository
public interface TikTokRelationshipRepository extends JpaRepository<TiktokRelationship, Long> {
    
}
