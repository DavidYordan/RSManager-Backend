package com.rsmanager.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.api.ServiceResponseDTO;
import com.rsmanager.dto.user.*;
import com.rsmanager.service.TbUserService;
import com.rsmanager.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;

import java.time.YearMonth;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TbUserService tbUserService;

    @PostMapping("/super_create")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<SuperCreateUserDTO>> superCreateUser(
            @Valid @RequestBody SuperCreateUserDTO request) {
        
        ServiceResponseDTO serviceResponseDTO = userService.superCreateUser(request);

        return ResponseEntity.ok(ApiResponseDTO.<SuperCreateUserDTO>builder()
                .success(serviceResponseDTO.isSuccess())
                .message(serviceResponseDTO.getMessage())
                .build());
    }

    /**
     * 获取当前用户的收益摘要
     */
    @PostMapping("/summary")
    public ResponseEntity<ApiResponseDTO<OwnerSummaryDTO>> getOwnerSummary(@RequestBody Map<String, String> request) {
        YearMonth selectedMonth = YearMonth.parse(request.get("selectedMonth"));
        OwnerSummaryDTO userSummary = userService.getOwnerSummary(selectedMonth);
        return ResponseEntity.ok(ApiResponseDTO.<OwnerSummaryDTO>builder()
                .success(true)
                .message("User summary retrieved successfully")
                .data(userSummary)
                .build());
    }

    /**
     * 检查邀请人是否存在
     */
    @PostMapping("/checkinviter")
    public ResponseEntity<ApiResponseDTO<?>> checkInviter(@Valid @RequestBody Map<String, String> request) {
        String inviter = request.get("username");
        Long userId = userService.findIdByUsername(inviter);
        //如果不存在则查询tbuser
        if (userId == null) {
            userId = tbUserService.findUserIdByPhone(inviter);
        }

        ApiResponseDTO<?> response = ApiResponseDTO.builder()
                .success(userId != null)
                .message(userId != null ? "Inviter found" : "Inviter not found")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户是否存在
     */
    @PostMapping("/exists")
    public ResponseEntity<ApiResponseDTO<?>> userExists(@Valid @RequestBody Map<String, String> request) {
        String username = request.get("username");
        Boolean exists = userService.userExists(username);
        ApiResponseDTO<?> response = ApiResponseDTO.builder()
                .success(exists)
                .message(exists ? "User exists" : "User does not exist")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 重置当前用户的密码
     */
    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponseDTO<Void>> resetPassword(
            @Valid @RequestBody BackendUserResetPasswordDTO request) {
        userService.resetPassword(request);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(true)
                .message("Password reset successfully")
                .build());
    }

    /**
     * 通用的用户搜索方法，支持分页
     */
    @PostMapping("/search")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<Page<SearchResponseDTO>>> searchUsers(
            @Valid @RequestBody BackendUserSearchDTO request) {

        Page<SearchResponseDTO> usersPage = userService.searchUsers(request);

        return ResponseEntity.ok(ApiResponseDTO.<Page<SearchResponseDTO>>builder()
                .success(true)
                .message("Users retrieved successfully")
                .data(usersPage)
                .build());
    }

    // public Optional<FindUserDTO> findUser(Long userId, String username, String fullname)

    /**
     * 根据user_id、username、fullname查询用户信息
     */
    @PostMapping("/finduser")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<FindUserDTO>> findUser(
            @Valid @RequestBody FindUserDTO request) {

        FindUserDTO user = userService.findUser(request)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(ApiResponseDTO.<FindUserDTO>builder()
                .success(true)
                .message("User retrieved successfully")
                .data(user)
                .build());
    }

    /**
     * 根据用户名查询用户ID
     */
    @PostMapping("/findIdByUsername")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> findIdByUsername(
            @Valid @RequestBody Map<String, String> request) {

        Long userId = userService.findIdByUsername(request.get("username"));

        return ResponseEntity.ok(ApiResponseDTO.<Map<String, Long>>builder()
                .success(true)
                .message("User ID retrieved successfully")
                .data(Collections.singletonMap("userId", userId))
                .build());
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<BackendUserUpdateDTO>> updateUser(
            @Valid @RequestBody BackendUserUpdateDTO request) {
        userService.updateUser(request);

        return ResponseEntity.ok(ApiResponseDTO.<BackendUserUpdateDTO>builder()
                .success(true)
                .message("User updated successfully")
                .build());
    }
}
