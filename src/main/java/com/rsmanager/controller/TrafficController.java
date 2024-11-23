package com.rsmanager.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.traffic.*;
import com.rsmanager.service.TrafficService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/traffic")
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;

    @PostMapping("/search")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<Page<SearchTrafficResponseDTO>>> searchUsers(@RequestBody SearchTrafficDTO request) {
        
        Page<SearchTrafficResponseDTO> searchResponse = trafficService.searchTraffic(request);

        return ResponseEntity.ok(ApiResponseDTO.<Page<SearchTrafficResponseDTO>>builder()
                .success(true)
                .message("Traffic search retrieved successfully")
                .data(searchResponse)
                .build());
    }
}
