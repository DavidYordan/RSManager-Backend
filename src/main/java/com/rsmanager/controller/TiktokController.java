package com.rsmanager.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.tbuser.*;
import com.rsmanager.service.TiktokAccountService;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/tiktok")
@RequiredArgsConstructor
public class TiktokController {

    private final TiktokAccountService tiktokAccountService;

    /**
     * 根据用户手机号查询用户ID
     *
     * @param request FindIdByPhoneRequest 包含用户手机号
     * @return ApiResponse 包含查询结果
     */
    @PostMapping("/checktiktok")
    public ResponseEntity<ApiResponseDTO<?>> checkTiktok(
            @Valid @RequestBody Map<String, String> request) {

        String tiktokAccount = request.get("tiktokAccount");
        Boolean result = tiktokAccountService.userExists(tiktokAccount);

        ApiResponseDTO<TbuserFindIdByPhoneResponseDTO> response = ApiResponseDTO.<TbuserFindIdByPhoneResponseDTO>builder()
                .success(!result)
                .message(!result ? "Tiktok account found" : "Tiktok account not found")
                .build();

        return ResponseEntity.ok(response);
    }
}
