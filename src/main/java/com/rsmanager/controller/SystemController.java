package com.rsmanager.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @GetMapping("/alltypes")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<GlobalParamsReponseDTO>> getAllTypes() {

        return ResponseEntity.ok(ApiResponseDTO.<GlobalParamsReponseDTO>builder()
                .success(true)
                .message("All global parameters retrieved successfully.")
                .data(systemService.getAllGlobalParams())
                .build());
    }

    // 获取所有地区参数
    @GetMapping("/allregions")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1, 2, 3)")
    public ResponseEntity<ApiResponseDTO<List<RegionProjectsDTO>>> getAllRegions() {

        return ResponseEntity.ok(ApiResponseDTO.<List<RegionProjectsDTO>>builder()
                .success(true)
                .message("All global parameters retrieved successfully.")
                .data(systemService.getAllRegionProjects())
                .build());
    }

    // 设置全局角色权限
    @PostMapping("/updaterolepermission")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<UpdateRolePermissionDTO>> updateRolePermission(@RequestBody UpdateRolePermissionDTO rolePermission) {
        
        UpdateRolePermissionDTO result = systemService.updateRolePermission(rolePermission);

        // 返回成功响应
        return ResponseEntity.ok(ApiResponseDTO.<UpdateRolePermissionDTO>builder()
                .success(true)
                .message("Role permissions set successfully.")
                .data(result)
                .build());
    }

    // 设置全局项目
    @PostMapping("/updateproject")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<UpdateProjectDTO>> updateProject(@RequestBody UpdateProjectDTO projectDTO) {
        
        UpdateProjectDTO result = systemService.updateProject(projectDTO);

        // 返回成功响应
        return ResponseEntity.ok(ApiResponseDTO.<UpdateProjectDTO>builder()
                .success(true)
                .message("Project information updated successfully.")
                .data(result)
                .build());
    }

    // 更新或添加区域信息
    @PostMapping("/updateregion")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<UpdateRegionDTO>> updateRegion(@RequestBody UpdateRegionDTO regionDTO) {
        
        UpdateRegionDTO result = systemService.updateRegion(regionDTO);

        // 返回成功响应
        return ResponseEntity.ok(ApiResponseDTO.<UpdateRegionDTO>builder()
                .success(true)
                .message("Project information updated successfully.")
                .data(result)
                .build());
    }

    // 删除deleteregion,要返回成功响应
    @PostMapping("/deleteregion")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteRegion(@RequestBody UpdateRegionDTO regionDTO) {
        Boolean isDeleted = systemService.deleteRegion(regionDTO.getRegionId());
        return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
                .success(isDeleted)
                .message(isDeleted ? "Region deleted successfully." : "Region deleted failed.")
                .build());
    }

//     // 设置项目信息
//     @PostMapping("/updateglobalparam")
//     @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
//     public ResponseEntity<ApiResponseDTO<Void>> setRolePermissions(@RequestBody UpdateProjectDTO rolePermissions) {
        
//         // 通过id找到对应的全局参数
//         // GlobalParams globalParams = globalParamsRepository.findById(rolePermissions.getParamId())
//         //         .orElseThrow(() -> new IllegalArgumentException("Cannot find the specified global parameter."));
//         // // 保留原有其它参数，只更新paramName、paramId

//         // globalParams.setParamName(rolePermissions.getParamName());
//         // globalParams.setParamValue(rolePermissions.getParamValue());
//         // globalParamsRepository.save(globalParams);

//         // 返回成功响应
//         return ResponseEntity.ok(ApiResponseDTO.<Void>builder()
//                 .success(true)
//                 .message("Project information updated successfully.")
//                 .build());
//     }

//     // 更新或添加区域信息
//     @PostMapping("/updateregion")
//     @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
//     public ResponseEntity<ApiResponseDTO<Long>> updateRegion(@RequestBody UpdateRegionDTO regionDTO) {
//         // 通过id找到对应的区域，如果不存在则新增
//         Region region = (regionDTO.getId() != null) ?
//             regionRepository.findById(regionDTO.getId()).orElseGet(Region::new) :
//             new Region();

//         region.setRegionCode(regionDTO.getRegionCode());
//         region.setRegionName(regionDTO.getRegionName());
//         region.setCurrency(regionDTO.getCurrency());
//         regionRepository.save(region);

//         // 返回成功响应，并包含新的id
//         ApiResponseDTO<Long> response = new ApiResponseDTO<>(
//                 true,
//                 "Region information updated successfully.",
//                 // region.getId(),
//                 null // 无分页信息时传入null
//         );

//         // 返回成功响应
//         return ResponseEntity.ok(response);}

    /**
     * 按筛选条件获取用户权限并分页
     *
     * @param filter SearchUserPermissionDTO 过滤条件
     * @return Page<UserPermissionDTO>
     */
    @PostMapping("/searchuserpermissions")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<Page<SearchUserPermissionsResponseDTO>>> searchUserPermissions(
            @RequestBody SearchUserPermissionDTO filter) {

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());
        Page<SearchUserPermissionsResponseDTO> result = systemService.searchUserPermissions(filter, pageable);
        
        return ResponseEntity.ok(ApiResponseDTO.<Page<SearchUserPermissionsResponseDTO>>builder()
                .success(true)
                .message("User permissions retrieved successfully.")
                .data(result)
                .build());
    }

    /**
     * 更新用户权限
     *
     * @param updateDTO UpdateUserPermissionDTO 更新信息
     * @return ApiResponseDTO<Void>
     */
    @PostMapping("/updateuserpermission")
    @PreAuthorize("@authServiceImpl.hasRoleIn(1)")
    public ResponseEntity<ApiResponseDTO<UpdateUserPermissionDTO>> updateUserPermission(
            @RequestBody UpdateUserPermissionDTO updateDTO) {

        UpdateUserPermissionDTO result = systemService.updateUserPermission(updateDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.<UpdateUserPermissionDTO>builder()
                .success(true)
                .message("User permission updated successfully.")
                .data(result)
                .build());
    }

}
