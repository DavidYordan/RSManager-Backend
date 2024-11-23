package com.rsmanager.service.impl;

import com.rsmanager.model.ApplicationFlowRecord;
import com.rsmanager.repository.local.ApplicationFlowRecordRepository;
import com.rsmanager.service.ApplicationFlowRecordService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationFlowRecordServiceImpl implements ApplicationFlowRecordService {
    
    private final ApplicationFlowRecordRepository applicationFlowRecordRepository;

    /**
    //      * 根据process_id获取申请状态
    //      *
    //      * @param process_id 状态值
    //      * @return List<ApplicationFlowRecord>
    //      */
    @Override
    public List<ApplicationFlowRecord> findByProcessId(Long process_id) {
        return applicationFlowRecordRepository.findByApplicationProcessRecordProcessId(process_id);
    }    
}
