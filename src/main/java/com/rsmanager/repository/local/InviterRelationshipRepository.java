package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rsmanager.model.BackendUser;
import com.rsmanager.model.InviterRelationship;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

@Repository
public interface InviterRelationshipRepository extends JpaRepository<InviterRelationship, Long>, JpaSpecificationExecutor<InviterRelationship> {
    
    @Query("SELECT ir FROM InviterRelationship ir WHERE ir.inviter.userId = :inviterUserId")
    List<InviterRelationship> findAllByInviterUserId(@Param("inviterUserId") Long inviterUserId);

    @Query("SELECT ir.user.userId FROM InviterRelationship ir WHERE ir.inviter.userId = :inviterUserId AND ir.status = true")
    Set<Long> findUserIdsByInviterUserId(@Param("inviterUserId") Long inviterUserId);

    @Query("SELECT ir FROM InviterRelationship ir WHERE ir.inviter.userId IN :inviterUserIds")
    List<InviterRelationship> findAllByInviterUserIdIn(@Param("inviterUserIds") Set<Long> inviterUserIds);

    @Query("SELECT COUNT(ir) FROM InviterRelationship ir WHERE ir.inviter.userId = :inviterId AND ir.status = true")
    Integer countByInviterId(Long inviterId);

    @Query("""
        SELECT DISTINCT u
        FROM BackendUser u
        INNER JOIN u.inviterRelationships ir
        INNER JOIN ir.user.applicationProcessRecordAsUser ap
        INNER JOIN ap.applicationPaymentRecords apr
        WHERE ir.inviter.userId = :inviterId
          AND apr.status = true
        """)
    List<BackendUser> findUserByInviterId(@Param("inviterId") Long inviterId);

    @Query("""
        SELECT DISTINCT u
        FROM BackendUser u
        INNER JOIN u.inviterRelationships ir
        INNER JOIN ir.user.applicationProcessRecordAsUser ap
        INNER JOIN ap.applicationPaymentRecords apr
        WHERE ir.inviter.userId IN :inviterIds
          AND apr.status = true
        """)
    List<BackendUser> findUserByInviterIds(@Param("inviterIds") Set<Long> inviterIds);
}
