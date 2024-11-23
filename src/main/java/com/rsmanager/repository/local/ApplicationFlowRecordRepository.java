package com.rsmanager.repository.local;

import com.rsmanager.model.ApplicationFlowRecord;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationFlowRecordRepository extends JpaRepository<ApplicationFlowRecord, Long> {
    List<ApplicationFlowRecord> findByApplicationProcessRecordProcessId(Long processId);
}
