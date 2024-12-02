package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.ApplicationProcessRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationProcessRecordRepository extends JpaRepository<ApplicationProcessRecord, Long>, JpaSpecificationExecutor<ApplicationProcessRecord>, ApplicationProcessRecordRepositoryCustom {
    
    Optional<ApplicationProcessRecord> findByFullname(String fullname);

    Optional<ApplicationProcessRecord> findByUsername(String username);

    @Query("SELECT a FROM ApplicationProcessRecord a WHERE a.inviter IS NULL")
    List<ApplicationProcessRecord> findAllByInviterIsNull();
}
