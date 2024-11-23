package com.rsmanager.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.tbuser.*;
import com.rsmanager.service.TbUserService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/tbuser")
@RequiredArgsConstructor
public class TbUserController {

    private final TbUserService tbUserService;

    /**
     * 根据用户手机号查询用户ID
     *
     * @param request FindIdByPhoneRequest 包含用户手机号
     * @return ApiResponse 包含查询结果
     */
    @PostMapping("/findIdByPhone")
    public ResponseEntity<ApiResponseDTO<TbuserFindIdByPhoneResponseDTO>> findIdByPhone(
            @Valid @RequestBody TbuserFindIdByPhoneRequestDTO request) {

        String platformAccount = request.getPlatformAccount();
        Long userId = tbUserService.findUserIdByPhone(platformAccount);

        TbuserFindIdByPhoneResponseDTO responseData = TbuserFindIdByPhoneResponseDTO.builder()
                .platformId(userId)
                .platformAccount(platformAccount)
                .build();

        ApiResponseDTO<TbuserFindIdByPhoneResponseDTO> response = ApiResponseDTO.<TbuserFindIdByPhoneResponseDTO>builder()
                .success(userId != null)
                .message(userId != null ? "User found" : "User not found")
                .data(responseData)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户ID列表获取用户信息列表
     *
     * @param request UserIdsRequest 包含用户ID列表
     * @return ApiResponse 包含用户信息列表
     */
    @PostMapping("/info")
    public ResponseEntity<ApiResponseDTO<List<TbuserInfoDTO>>> getUserInfoList(
            @Valid @RequestBody TbuserUserIdsRequestDTO request) {

        List<TbuserInfoDTO> userInfoList = tbUserService.getUserInfoListByIds(request.getUserIds());

        ApiResponseDTO<List<TbuserInfoDTO>> response = ApiResponseDTO.<List<TbuserInfoDTO>>builder()
                .success(!userInfoList.isEmpty())
                .message(!userInfoList.isEmpty() ? "Users found" : "Users not found")
                .data(userInfoList)
                .build();


        return ResponseEntity.ok(response);
    }
}
