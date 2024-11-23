package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.ApplicationProcessRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationProcessRecordRepository extends JpaRepository<ApplicationProcessRecord, Long>, JpaSpecificationExecutor<ApplicationProcessRecord> {
    
    List<ApplicationProcessRecord> findByUserId(Long userId);
    @Query("SELECT a.inviterName FROM ApplicationProcessRecord a WHERE a.userId = :userId")
    Optional<String> findInviterNameByUserId(Long userId);
    List<ApplicationProcessRecord> findByUsername(String username);
    List<ApplicationProcessRecord> findByFullname(String fullname);
    List<ApplicationProcessRecord> findByPlatformId(Long platformId);
    List<ApplicationProcessRecord> findAllByInviterId(Long inviterId);
    List<ApplicationProcessRecord> findAllByInviterName(String inviterName);
    List<ApplicationProcessRecord> findAllByInviterFullname(String inviterFullname);
    List<ApplicationProcessRecord> findAllByManagerId(Long managerId);
    List<ApplicationProcessRecord> findAllByManagerName(String managerName);
    List<ApplicationProcessRecord> findAllByManagerFullname(String managerFullname);
}
