package com.rsmanager.repository.local;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokRelationship;

@Repository
public interface TikTokRelationshipRepository extends JpaRepository<TiktokRelationship, Integer> {
    
    Optional<TiktokRelationship> findByTiktokAccount(String tiktokAccount);

    @Query("SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END FROM TiktokRelationship tr WHERE tr.tiktokAccount = :tiktokAccount AND tr.status = true")
    Boolean isTiktokAccountExists(String tiktokAccount);

    @Query("SELECT tr FROM TiktokRelationship tr WHERE tr.tiktokAccount = :tiktokAccount AND tr.status = true")
    Optional<TiktokRelationship> findByTiktokAccountAndStatus(String tiktokAccount);
}
