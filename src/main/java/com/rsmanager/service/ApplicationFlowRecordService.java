package com.rsmanager.service;

import java.util.List;

import com.rsmanager.model.ApplicationFlowRecord;

public interface ApplicationFlowRecordService {

    /**
     * 根据process_id获取申请状态
     *
     * @param process_id 状态值
     * @return ApplicationFlowRecord
     */
    List<ApplicationFlowRecord> findByProcessId(Long process_id);
}
