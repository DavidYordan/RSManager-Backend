package com.rsmanager.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.service.TiktokService;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/tiktok")
@RequiredArgsConstructor
public class TiktokController {

    private final TiktokService tiktokService;

    /**
     * 根据用户手机号查询用户ID
     */
    @PostMapping("/checktiktok")
    public ResponseEntity<ApiResponseDTO<?>> checkTiktok(
            @Valid @RequestBody Map<String, String> request) {

        String message = tiktokService.userExists(request.get("tiktokAccount"));

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(message == "success")
                .message(message)
                .build());
    }
}
