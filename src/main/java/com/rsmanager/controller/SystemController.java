package com.rsmanager.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.system.*;
import com.rsmanager.service.SystemService;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    // 获取所有全局参数
    @GetMapping("/allglobalparams")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<GlobalParamsReponseDTO>> getAllGlobalParams() {

        GlobalParamsReponseDTO result = systemService.getAllGlobalParams();

        return ResponseEntity.ok(ApiResponseDTO.<GlobalParamsReponseDTO>builder()
                .success(true)
                .message("All global parameters retrieved successfully.")
                .data(result)
                .build());
    }

    // 获取所有默认项目参数
    @GetMapping("/allprojects")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<List<ProjectDTO>>> getAllProjects() {

        List<ProjectDTO> result = systemService.getAllProjects();

        return ResponseEntity.ok(ApiResponseDTO.<List<ProjectDTO>>builder()
                .success(true)
                .message("All projects retrieved successfully.")
                .data(result)
                .build());
    }

    // 获取所有默认地区参数
    @GetMapping("/allregionscurrencies")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<List<RegionCurrencyDTO>>> getAllRegionCurrencies() {

        List<RegionCurrencyDTO> result = systemService.getAllRegionCurrencies();

        return ResponseEntity.ok(ApiResponseDTO.<List<RegionCurrencyDTO>>builder()
                .success(true)
                .message("All region currencies retrieved successfully.")
                .data(result)
                .build());
    }

    // 获取所有地区项目参数
    @GetMapping("/allregionprojects")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3, 8)")
    public ResponseEntity<ApiResponseDTO<List<RegionProjectsDTO>>> getAllRegionProjects() {

        List<RegionProjectsDTO> result = systemService.getAllRegionProjects();

        return ResponseEntity.ok(ApiResponseDTO.<List<RegionProjectsDTO>>builder()
                .success(true)
                .message("All region projects retrieved successfully.")
                .data(result)
                .build());
    }

    // 设置全局项目
    @PostMapping("/updateproject")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> updateProject(@RequestBody UpdateProjectDTO projectDTO) {
        
        Boolean result = systemService.updateProject(projectDTO);

        // 返回成功响应
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Project information updated successfully." : "Project information updated failed.")
                .build());
    }

    // 更新或添加区域信息
    @PostMapping("/updateregion")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> updateRegionProjects(@RequestBody List<UpdateRegionProjectsDTO> regionDTOs) {
        
        Boolean result = systemService.updateRegionProjects(regionDTOs);

        // 返回成功响应
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Region information updated successfully." : "Region information updated failed.")
                .build());
    }

    // 删除deleteregion,要返回成功响应
    @PostMapping("/deleteregionprojects")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> deleteRegionProjects(@RequestBody List<UpdateRegionProjectsDTO> regionDTOs) {
        
        Boolean result = systemService.deleteRegionProjects(regionDTOs);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(result)
                .message(result ? "Region deleted successfully." : "Region deleted failed.")
                .build());
    }

    /**
     * 按筛选条件获取用户权限并分页
     */
    @PostMapping("/searchuserpermissions")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<Page<SearchRolePermissionRelationshipResponseDTO>>> searchRolePermissionRelationships(
            @RequestBody SearchRolePermissionRelationshipDTO request) {

        Page<SearchRolePermissionRelationshipResponseDTO> result = systemService.searchRolePermissionRelationships(request);
        
        return ResponseEntity.ok(ApiResponseDTO.<Page<SearchRolePermissionRelationshipResponseDTO>>builder()
                .success(true)
                .message("User permissions retrieved successfully.")
                .data(result)
                .build());
    }

    /**
     * 更新用户权限
     */
    @PostMapping("/updateuserpermissionrelationship")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> updateUserPermissionRelationship(
            @RequestBody UpdateRolePermissionRelationshipDTO updateDTO) {

        Boolean result = systemService.updateUserPermissionRelationship(updateDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "User permission updated successfully." : "User permission updated failed.")
                .build());
    }

}
