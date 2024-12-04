package com.rsmanager.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.rsmanager.dto.system.*;
import com.rsmanager.dto.user.SearchRolePermissionsDTO;
import com.rsmanager.dto.user.SearchRolePermissionsResponseDTO;

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
     * 更新全局项目
     */
    Boolean updateProject(UpdateProjectDTO updateDTO);

    /**
     * 删除全局项目
     */
    Boolean deleteProject(UpdateProjectDTO updateDTO);

    /**
     * 添加项目
     */
    UpdateProjectDTO addProject(UpdateProjectDTO updateDTO);

    /**
     * 更新地区货币
     */
    Boolean updateRegionCurrency(UpdateRegionCurrencyDTO updateDTO);

    /**
     * 删除地区货币
     */
    Boolean deleteRegionCurrency(UpdateRegionCurrencyDTO updateDTO);

    /**
     * 添加地区货币
     */
    UpdateRegionCurrencyDTO addRegionCurrency(UpdateRegionCurrencyDTO updateDTO);

    /**
     * 更新地区项目
     */
    Boolean updateRegionProjects(List<UpdateRegionProjectsDTO> request);

    /**
     * 删除地区项目
     */
    Boolean deleteRegionProjects(List<UpdateRegionProjectsDTO> request);

    /**
     * 添加地区项目
     */
    List<UpdateRegionProjectsDTO> addRegionProjects(List<UpdateRegionProjectsDTO> request);
    
    /**
     * 按筛选条件获取用户权限并分页
     */
    Page<SearchRolePermissionsResponseDTO> searchRolePermissions(SearchRolePermissionsDTO request);

    /**
     * 更新用户权限
     */
    Boolean updateRolePermission(UpdateRolePermissionDTO updateDTO);
}
