package com.rsmanager.repository.local;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.TiktokRelationship;

@Repository
public interface TikTokRelationshipRepository extends JpaRepository<TiktokRelationship, Integer> {
    
    Optional<TiktokRelationship> findByTiktokAccount(String tiktokAccount);

    // 判断tiktokAccount是否存在，status为true,存在则返回true

    @Query("SELECT CASE WHEN COUNT(tr) > 0 THEN true ELSE false END FROM TiktokRelationship tr WHERE tr.tiktokAccount = :tiktokAccount AND tr.status = true")
    Boolean isTiktokAccountExists(String tiktokAccount);
}
