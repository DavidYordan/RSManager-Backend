package com.rsmanager.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.rsmanager.dto.system.*;

public interface SystemService {

    /**
     * 获取所有全局参数
     */
    GlobalParamsReponseDTO getAllGlobalParams();

    /**
     * 获取所有默认项目参数
     */
    List<ProjectDTO> getAllProjects();

    /**
     * 获取所有默认地区参数
     */
    List<RegionCurrencyDTO> getAllRegionCurrencies();

    /**
     * 获取所有项目参数
     */
    List<RegionProjectsDTO> getAllRegionProjects();

    /**
     * 更新默认项目
     */
    Boolean updateProject(UpdateProjectDTO updateDTO);

    /**
     * 更新或添加项目
     */
    Boolean updateRegionProjects(List<UpdateRegionProjectsDTO> request);

    /**
     * 删除项目
     */
    Boolean deleteRegionProjects(List<UpdateRegionProjectsDTO> request);
    
    /**
     * 按筛选条件获取用户权限并分页
     */
    Page<SearchRolePermissionRelationshipResponseDTO> searchRolePermissionRelationships(SearchRolePermissionRelationshipDTO request);

    /**
     * 更新用户权限
     */
    Boolean updateUserPermissionRelationship(UpdateRolePermissionRelationshipDTO updateDTO);
}
