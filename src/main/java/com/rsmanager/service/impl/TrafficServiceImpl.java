package com.rsmanager.service.impl;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rsmanager.dto.traffic.*;
import com.rsmanager.repository.local.BackendUserRepository;
import com.rsmanager.service.TrafficService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrafficServiceImpl implements TrafficService {

    private final BackendUserRepository backendUserRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<SearchTrafficResponseDTO> searchTraffic(SearchTrafficDTO request) {

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("userId").ascending());

        return backendUserRepository.searchTraffics(request, pageable);
    }
}
