package com.rsmanager.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rsmanager.dto.system.*;
import com.rsmanager.dto.user.SearchRolePermissionsDTO;
import com.rsmanager.dto.user.SearchRolePermissionsResponseDTO;
import com.rsmanager.model.Project;
import com.rsmanager.model.RegionCurrency;
import com.rsmanager.model.RegionProject;
import com.rsmanager.model.RolePermissionRelationship;
import com.rsmanager.repository.local.BackendUserRepository;
import com.rsmanager.repository.local.ProjectRepository;
import com.rsmanager.repository.local.RegionCurrencyRepository;
import com.rsmanager.repository.local.RegionProjectRepository;
import com.rsmanager.repository.local.RolePermissionRelationshipRepository;
import com.rsmanager.repository.local.UsdRateRepository;
import com.rsmanager.service.SystemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemServerImpl implements SystemService {

    private final BackendUserRepository backendUserRepository;
    private final ProjectRepository projectRepository;
    private final RegionProjectRepository regionProjectRepository;
    private final RegionCurrencyRepository regionCurrencyRepository;
    private final RolePermissionRelationshipRepository rolePermissionRelationshipRepository;
    private final UsdRateRepository usdRateRepository;

    /**
     * 获取所有全局参数
     */
    @Override
    @Transactional(readOnly = true)
    public GlobalParamsReponseDTO getAllGlobalParams() {
        return GlobalParamsReponseDTO.builder()
            .regionCurrencyDTOs(getAllRegionCurrencies())
            .regionProjectsDTOs(getAllRegionProjects())
            .projectDTOs(getAllProjects())
            .build();
    }

    /**
     * 获取所有默认项目参数
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
            .map(p -> {
                return ProjectDTO.builder()
                    .projectId(p.getProjectId())
                    .roleId(p.getRoleId())
                    .projectName(p.getProjectName())
                    .projectAmount(p.getProjectAmount())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取所有项目参数
     */
    @Override
    @Transactional(readOnly = true)
    public List<RegionProjectsDTO> getAllRegionProjects() {
        return regionProjectRepository.getAllRegionProjects();
    }

    /**
     * 获取所有默认地区参数
     */
    @Override
    @Transactional(readOnly = true)
    public List<RegionCurrencyDTO> getAllRegionCurrencies() {
        return regionCurrencyRepository.findAll().stream()
            .map(rc -> {
                String currencyCode = rc.getCurrencyCode();
                Double rate = usdRateRepository.findRateByDateAndCurrencyCode(
                    LocalDate.of(1970, 1, 1), currencyCode)
                    .orElse(0.0);
                return RegionCurrencyDTO.builder()
                    .regionCode(rc.getRegionCode())
                    .regionName(rc.getRegionName())
                    .currencyName(rc.getCurrencyName())
                    .currencyCode(currencyCode)
                    .rate(rate)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 更新默认项目
     */
    @Override
    @Transactional
    public Boolean updateProject(UpdateProjectDTO updateProjectDTO) {

        Project project = projectRepository.findById(updateProjectDTO.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("RegionProject not found for projectId: " + updateProjectDTO.getProjectId()));
        
        project.setRoleId(updateProjectDTO.getRoleId());
        project.setProjectName(updateProjectDTO.getProjectName());
        project.setProjectAmount(updateProjectDTO.getProjectAmount());

        projectRepository.save(project);

        return true;
    }

    /**
     * 删除默认项目
     */
    @Override
    @Transactional
    public Boolean deleteProject(UpdateProjectDTO updateProjectDTO) {

        Project project = projectRepository.findById(updateProjectDTO.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("RegionProject not found for projectId: " + updateProjectDTO.getProjectId()));
        
        projectRepository.delete(project);

        return true;
    }

    /**
     * 添加默认项目
     */
    @Override
    @Transactional
    public UpdateProjectDTO addProject(UpdateProjectDTO updateProjectDTO) {

        Project project = Project.builder()
            .roleId(updateProjectDTO.getRoleId())
            .projectName(updateProjectDTO.getProjectName())
            .projectAmount(updateProjectDTO.getProjectAmount())
            .build();

        Project savedProject = projectRepository.save(project);

        return UpdateProjectDTO.builder()
            .projectId(savedProject.getProjectId())
            .roleId(savedProject.getRoleId())
            .projectName(savedProject.getProjectName())
            .projectAmount(savedProject.getProjectAmount())
            .build();
    }

    /**
     * 更新地区货币
     */
    @Override
    @Transactional
    public Boolean updateRegionCurrency(UpdateRegionCurrencyDTO updateRegionCurrencyDTO) {

        RegionCurrency regionCurrency = regionCurrencyRepository.findById(updateRegionCurrencyDTO.getRegionName())
                .orElseThrow(() -> new IllegalArgumentException("RegionCurrency not found for regionCode: " + updateRegionCurrencyDTO.getRegionCode()));
        
        regionCurrency.setRegionCode(updateRegionCurrencyDTO.getRegionCode());
        regionCurrency.setCurrencyCode(updateRegionCurrencyDTO.getCurrencyCode());
        regionCurrency.setCurrencyName(updateRegionCurrencyDTO.getCurrencyName());

        regionCurrencyRepository.save(regionCurrency);

        return true;
    }

    /**
     * 删除地区货币
     */
    @Override
    @Transactional
    public Boolean deleteRegionCurrency(UpdateRegionCurrencyDTO updateRegionCurrencyDTO) {

        RegionCurrency regionCurrency = regionCurrencyRepository.findById(updateRegionCurrencyDTO.getRegionName())
                .orElseThrow(() -> new IllegalArgumentException("RegionCurrency not found for regionCode: " + updateRegionCurrencyDTO.getRegionCode()));
        
        regionCurrencyRepository.delete(regionCurrency);

        return true;
    }

    /**
     * 添加地区货币
     */
    @Override
    @Transactional
    public UpdateRegionCurrencyDTO addRegionCurrency(UpdateRegionCurrencyDTO updateRegionCurrencyDTO) {

        // 检查是否已存在
        regionCurrencyRepository.findById(updateRegionCurrencyDTO.getRegionName())
            .ifPresent(rc -> {
                throw new IllegalArgumentException("RegionCurrency already exists for regionCode: " + updateRegionCurrencyDTO.getRegionCode());
            });

        RegionCurrency regionCurrency = RegionCurrency.builder()
            .regionCode(updateRegionCurrencyDTO.getRegionCode())
            .regionName(updateRegionCurrencyDTO.getRegionName())
            .currencyCode(updateRegionCurrencyDTO.getCurrencyCode())
            .currencyName(updateRegionCurrencyDTO.getCurrencyName())
            .build();

        RegionCurrency savedRegionCurrency = regionCurrencyRepository.save(regionCurrency);

        return UpdateRegionCurrencyDTO.builder()
            .regionCode(savedRegionCurrency.getRegionCode())
            .regionName(savedRegionCurrency.getRegionName())
            .currencyCode(savedRegionCurrency.getCurrencyCode())
            .currencyName(savedRegionCurrency.getCurrencyName())
            .build();
    }

    /**
     * 更新地区项目
     */
    @Override
    @Transactional
    public Boolean updateRegionProjects(List<UpdateRegionProjectsDTO> request) {

        for (UpdateRegionProjectsDTO updateRegionProjectsDTO : request) {
            RegionProject regionProject = regionProjectRepository.findById(RegionProject.RegionProjectId.builder()
                .regionCode(updateRegionProjectsDTO.getRegionCode())
                .currencyCode(updateRegionProjectsDTO.getCurrencyCode())
                .projectId(updateRegionProjectsDTO.getProjectId())
                .build())
                .orElseThrow(() -> new IllegalArgumentException("RegionProject not found for regionCode: " + updateRegionProjectsDTO.getRegionCode() + ", currencyCode: " + updateRegionProjectsDTO.getCurrencyCode() + ", projectId: " + updateRegionProjectsDTO.getProjectId()));
            
            regionProject.setProjectName(updateRegionProjectsDTO.getProjectName());
            regionProject.setProjectAmount(updateRegionProjectsDTO.getProjectAmount());

            regionProjectRepository.save(regionProject);
        }
    
        return true;
    }

    /**
     * 删除地区项目
     */
    @Override
    @Transactional
    public Boolean deleteRegionProjects(List<UpdateRegionProjectsDTO> request) {

        for (UpdateRegionProjectsDTO updateRegionProjectsDTO : request) {
            RegionProject regionProject = regionProjectRepository.findById(RegionProject.RegionProjectId.builder()
                .regionCode(updateRegionProjectsDTO.getRegionCode())
                .currencyCode(updateRegionProjectsDTO.getCurrencyCode())
                .projectId(updateRegionProjectsDTO.getProjectId())
                .build())
                .orElseThrow(() -> new IllegalArgumentException("RegionProject not found for regionCode: " + updateRegionProjectsDTO.getRegionCode() + ", currencyCode: " + updateRegionProjectsDTO.getCurrencyCode() + ", projectId: " + updateRegionProjectsDTO.getProjectId()));
            
            regionProjectRepository.delete(regionProject);
        }
    
        return true;
    }

    /**
     * 添加地区项目
     */
    @Override
    @Transactional
    public List<UpdateRegionProjectsDTO> addRegionProjects(List<UpdateRegionProjectsDTO> request) {

        List<UpdateRegionProjectsDTO> response = new ArrayList<>();

        for (UpdateRegionProjectsDTO updateRegionProjectsDTO : request) {
            RegionProject regionProject = RegionProject.builder()
                .id(RegionProject.RegionProjectId.builder()
                    .regionCode(updateRegionProjectsDTO.getRegionCode())
                    .currencyCode(updateRegionProjectsDTO.getCurrencyCode())
                    .projectId(updateRegionProjectsDTO.getProjectId())
                    .build())
                .regionName(updateRegionProjectsDTO.getRegionName())
                .currencyName(updateRegionProjectsDTO.getCurrencyName())
                .projectName(updateRegionProjectsDTO.getProjectName())
                .projectAmount(updateRegionProjectsDTO.getProjectAmount())
                .build();

            RegionProject savedRegionProject = regionProjectRepository.save(regionProject);

            response.add(UpdateRegionProjectsDTO.builder()
                .regionCode(savedRegionProject.getId().getRegionCode())
                .currencyCode(savedRegionProject.getId().getCurrencyCode())
                .projectId(savedRegionProject.getId().getProjectId())
                .regionName(savedRegionProject.getRegionName())
                .currencyName(savedRegionProject.getCurrencyName())
                .projectName(savedRegionProject.getProjectName())
                .projectAmount(savedRegionProject.getProjectAmount())
                .build());
        }
    
        return response;
    }

    /**
     * 搜索用户权限
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SearchRolePermissionsResponseDTO> searchRolePermissions(SearchRolePermissionsDTO request) {
        
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("userId").ascending());

        return backendUserRepository.searchRolePermissions(request, pageable);
    }

    /**
     * 更新用户权限
     */
    @Override
    @Transactional
    public Boolean updateRolePermission(UpdateRolePermissionDTO updateDTO) {
        
        RolePermissionRelationship entity = rolePermissionRelationshipRepository.findById(updateDTO.getRecordId())
                .orElseThrow(() -> new IllegalArgumentException("RolePermissionRelationship not found for id: " + updateDTO.getRecordId()));

        entity.setRate1(updateDTO.getRate1());
        entity.setRate2(updateDTO.getRate2());
        entity.setStartDate(updateDTO.getStartDate());
        entity.setEndDate(updateDTO.getEndDate());
        entity.setStatus(updateDTO.getStatus());

        rolePermissionRelationshipRepository.save(entity);

        return true;
    }
}
