package com.rsmanager.repository.local;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rsmanager.model.ApplicationPaymentRecord;
import com.rsmanager.model.BackendUser;

import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationPaymentRecordRepository extends JpaRepository<ApplicationPaymentRecord, Long>, JpaSpecificationExecutor<ApplicationPaymentRecord> {
    
    @Query("""
        SELECT DISTINCT u
        FROM BackendUser u
        INNER JOIN u.applicationProcessRecordAsUser ap
        INNER JOIN ap.applicationPaymentRecords apr
        WHERE u.userId IN :userIds
          AND apr.status = true
        """)
    List<BackendUser> findByUserIds(@Param("userIds") Set<Long> userIds);
}
