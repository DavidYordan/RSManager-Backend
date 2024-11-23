package com.rsmanager.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rsmanager.dto.system.*;

public interface SystemService {

    /**
     * 根据分类获取全局参数
     *
     * @param classify 分类
     * @return GlobalParamsDTO
     */
    // GlobalParamsReponseDTO getParamsByClassify(String classify);

    /**
     * 获取所有全局参数
     *
     * @return GlobalParamsDTO
     */
    GlobalParamsReponseDTO getAllGlobalParams();

    /**
     * 获取所有地区参数
     *
     * @return RegionDTO
     */
    List<RegionDTO> getAllRegions();

    
    /**
     * 按筛选条件获取用户权限并分页
     *
     * @param filter   SearchUserPermissionDTO 过滤条件
     * @param pageable 分页信息
     * @return Page<UserPermissionDTO>
     */
    Page<SearchUserPermissionsResponseDTO> searchUserPermissions(SearchUserPermissionDTO filter, Pageable pageable);

    /**
     * 更新用户权限
     *
     * @param updateDTO UpdateUserPermissionDTO 更新信息
     * @return UpdateUserPermissionDTO
     */
    UpdateUserPermissionDTO updateUserPermission(UpdateUserPermissionDTO updateDTO);


    /**
     * 更新角色权限
     *
     * @param updateDTO UpdateRolePermissionDTO 更新信息
     */
    UpdateRolePermissionDTO updateRolePermission(UpdateRolePermissionDTO updateDTO);

    /**
     * 更新项目
     *
     * @param updateDTO UpdateProjectDTO 更新信息
     * @return UpdateProjectDTO
     */
    UpdateProjectDTO updateProject(UpdateProjectDTO updateDTO);

    /**
     * 更新或添加区域信息
     * 
     * @param regionDTO UpdateRegionDTO 区域信息
     * @return UpdateRegionDTO
     */
    UpdateRegionDTO updateRegion(UpdateRegionDTO regionDTO);
    

    /**
     * 删除区域
     *
     * @param regionId 区域ID
     */
    Boolean deleteRegion(Integer regionId);
}
