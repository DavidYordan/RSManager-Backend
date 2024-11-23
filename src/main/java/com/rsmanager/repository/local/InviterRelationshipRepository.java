package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rsmanager.model.InviterRelationship;

import java.util.List;
import java.util.Set;

public interface InviterRelationshipRepository extends JpaRepository<InviterRelationship, Long>, JpaSpecificationExecutor<InviterRelationship> {
    
    @Query("SELECT ir FROM InviterRelationship ir WHERE ir.inviter.userId = :inviterUserId")
    List<InviterRelationship> findAllByInviterUserId(@Param("inviterUserId") Long inviterUserId);


    @Query("SELECT ir FROM InviterRelationship ir WHERE ir.inviter.userId IN :inviterUserIds")
    List<InviterRelationship> findAllByInviterUserIdIn(@Param("inviterUserIds") Set<Long> inviterUserIds);

}
