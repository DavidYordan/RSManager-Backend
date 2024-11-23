package com.rsmanager.service;

import org.springframework.data.domain.Page;

import com.rsmanager.dto.traffic.SearchTrafficDTO;
import com.rsmanager.dto.traffic.SearchTrafficResponseDTO;

public interface TrafficService {

    /**
     * 根据查询条件搜索流量，支持分页
     *
     * @param request
     * @return
     */
    Page<SearchTrafficResponseDTO> searchTraffic( SearchTrafficDTO request);
    
}
