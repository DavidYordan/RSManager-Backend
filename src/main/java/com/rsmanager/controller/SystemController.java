package com.rsmanager.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.rsmanager.dto.api.ApiResponseDTO;
import com.rsmanager.dto.system.*;
import com.rsmanager.dto.user.SearchRolePermissionsDTO;
import com.rsmanager.dto.user.SearchRolePermissionsResponseDTO;
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

    // 更新全局项目
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

    // 删除全局项目
    @PostMapping("/deleteproject")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> deleteProject(@RequestBody UpdateProjectDTO projectDTO) {
        
        Boolean result = systemService.deleteProject(projectDTO);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(result)
                .message(result ? "Project deleted successfully." : "Project deleted failed.")
                .build());
    }

    // 新增全局项目
    @PostMapping("/addproject")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<UpdateProjectDTO>> addProject(@RequestBody UpdateProjectDTO projectDTO) {
        
        UpdateProjectDTO result = systemService.addProject(projectDTO);

        return ResponseEntity.ok(ApiResponseDTO.<UpdateProjectDTO>builder()
                .success(true)
                .message("Project added successfully.")
                .data(result)
                .build());
    }

    // 更新地区币种
    @PostMapping("/updateregioncurrency")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> updateRegionCurrency(@RequestBody UpdateRegionCurrencyDTO regionCurrencyDTO) {
        
        Boolean result = systemService.updateRegionCurrency(regionCurrencyDTO);

        // 返回成功响应
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Region currency updated successfully." : "Region currency updated failed.")
                .build());
    }

    // 删除地区币种
    @PostMapping("/deleteregioncurrency")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> deleteRegionCurrency(@RequestBody UpdateRegionCurrencyDTO regionCurrencyDTO) {
        
        Boolean result = systemService.deleteRegionCurrency(regionCurrencyDTO);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(result)
                .message(result ? "Region currency deleted successfully." : "Region currency deleted failed.")
                .build());
    }

    // 新增地区币种
    @PostMapping("/addregioncurrency")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<UpdateRegionCurrencyDTO>> addRegionCurrency(@RequestBody UpdateRegionCurrencyDTO regionCurrencyDTO) {
        
        UpdateRegionCurrencyDTO result = systemService.addRegionCurrency(regionCurrencyDTO);

        return ResponseEntity.ok(ApiResponseDTO.<UpdateRegionCurrencyDTO>builder()
                .success(true)
                .message("Region currency added successfully.")
                .data(result)
                .build());
    }

    // 更新地区项目信息
    @PostMapping("/updateregionprojects")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> updateRegionProjects(@RequestBody List<UpdateRegionProjectsDTO> regionDTOs) {
        
        Boolean result = systemService.updateRegionProjects(regionDTOs);

        // 返回成功响应
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "Region information updated successfully." : "Region information updated failed.")
                .build());
    }

    // 删除地区项目信息
    @PostMapping("/deleteregionprojects")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> deleteRegionProjects(@RequestBody List<UpdateRegionProjectsDTO> regionDTOs) {
        
        Boolean result = systemService.deleteRegionProjects(regionDTOs);

        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(result)
                .message(result ? "Region deleted successfully." : "Region deleted failed.")
                .build());
    }

    // 新增地区项目信息
    @PostMapping("/addregionprojects")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<List<UpdateRegionProjectsDTO>>> addRegionProjects(@RequestBody List<UpdateRegionProjectsDTO> regionDTOs) {
        
        List<UpdateRegionProjectsDTO> result = systemService.addRegionProjects(regionDTOs);

        return ResponseEntity.ok(ApiResponseDTO.<List<UpdateRegionProjectsDTO>>builder()
                .success(true)
                .message("Region added successfully.")
                .data(result)
                .build());
    }

    /**
     * 按筛选条件获取用户权限并分页
     */
    @PostMapping("/searchrolepermissions")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<Page<SearchRolePermissionsResponseDTO>>> searchRolePermissionRelationships(
            @RequestBody SearchRolePermissionsDTO request) {

        Page<SearchRolePermissionsResponseDTO> result = systemService.searchRolePermissions(request);
        
        return ResponseEntity.ok(ApiResponseDTO.<Page<SearchRolePermissionsResponseDTO>>builder()
                .success(true)
                .message("Role permissions retrieved successfully.")
                .data(result)
                .build());
    }

    /**
     * 更新用户权限
     */
    @PostMapping("/updaterolepermission")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<?>> updateRolePermission(
            @RequestBody UpdateRolePermissionDTO updateDTO) {

        Boolean result = systemService.updateRolePermission(updateDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(result)
                .message(result ? "User permission updated successfully." : "User permission updated failed.")
                .build());
    }

}
