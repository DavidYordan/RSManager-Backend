package com.rsmanager.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.rsmanager.dto.traffic.*;
import com.rsmanager.repository.local.BackendUserRepository;
import com.rsmanager.repository.local.TikTokRelationshipRepository;
import com.rsmanager.repository.local.TiktokAccountRepository;
import com.rsmanager.service.TrafficService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrafficServiceImpl implements TrafficService {

    private final BackendUserRepository backendUserRepository;
    private final TiktokAccountRepository tiktokAccountRepository;
    private final TikTokRelationshipRepository tikTokRelationshipRepository;

    @Override
    public Page<SearchTrafficResponseDTO> searchTraffic(SearchTrafficDTO request) {
        return null;
    }
    
}
