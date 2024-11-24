package com.rsmanager.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import com.rsmanager.dto.system.*;
import com.rsmanager.model.BackendRole;
import com.rsmanager.model.BackendUser;
import com.rsmanager.model.PermissionRelationship;
import com.rsmanager.model.Project;
import com.rsmanager.model.RegionCurrency;
import com.rsmanager.model.RegionProject;
import com.rsmanager.model.RolePermission;
import com.rsmanager.model.RoleRelationship;
import com.rsmanager.repository.local.BackendRoleRepository;
import com.rsmanager.repository.local.ProjectRepository;
import com.rsmanager.repository.local.RegionCurrencyRepository;
import com.rsmanager.repository.local.RegionProjectRepository;
import com.rsmanager.repository.local.PermissionRelationshipRepository;
import com.rsmanager.repository.local.RolePermissionRepository;
import com.rsmanager.service.SystemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemServerImpl implements SystemService {

    private final BackendRoleRepository backendRoleRepository;
    private final PermissionRelationshipRepository permissionRelationshipRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ProjectRepository projectRepository;
    private final RegionProjectRepository regionProjectRepository;
    private final RegionCurrencyRepository regionCurrencyRepository;

    @Override
    @Transactional(readOnly = true)
    public GlobalParamsReponseDTO getAllGlobalParams() {

        // 构建 projectDTO 列表
        List<ProjectDTO> projectDTOs = getAllProjects();

        // 构建 regionCurrencyDTO 列表
        List<RegionCurrencyDTO> regionCurrencyDTOs = getAllRegionCurrencies();

        // 构建 regionProjectsDTO 列表
        List<RegionProjectsDTO> regionProjectsDTOs = getAllRegionProjects();

        // 构建 RolePermissionDTO 列表
        List<BackendRole> backendRoles = backendRoleRepository.findAll();
        List<RolePermissionDTO> rolePermissionDTOs = backendRoles.stream()
            .map(role -> {
                List<RolePermissionDTO.PermissionDTO> permissionDTOs = role.getRolePermissions().stream()
                    .map(rolePermission -> RolePermissionDTO.PermissionDTO.builder()
                        .permissionId(rolePermission.getPermission().getPermissionId())
                        .rate1(rolePermission.getRate1())
                        .rate2(rolePermission.getRate2())
                        .isEnabled(rolePermission.getIsEnabled())
                        .build()
                    ).collect(Collectors.toList());

                return RolePermissionDTO.builder()
                    .roleId(role.getRoleId())
                    .roleName(role.getRoleName())
                    .permissionDTOs(permissionDTOs)
                    .build();
            })
            .collect(Collectors.toList());

        // 构建最终的 GlobalParamsReponseDTO
        GlobalParamsReponseDTO responseDTO = GlobalParamsReponseDTO.builder()
            .regionCurrencyDTOs(regionCurrencyDTOs)
            .regionProjectsDTOs(regionProjectsDTOs)
            .projectDTOs(projectDTOs)
            .rolePermissionDTOs(rolePermissionDTOs)
            .build();

        return responseDTO;
    }

    /**
     * 获取所有默认项目参数
     *
     * @return ProjectDTO
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
            .map(p -> {
                return ProjectDTO.builder()
                    .projectId(p.getProjectId())
                    .projectName(p.getProjectName())
                    .projectAmount(p.getProjectAmount())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取所有项目参数
     *
     * @return RegionProjectsDTO
     */
    @Override
    @Transactional(readOnly = true)
    public List<RegionProjectsDTO> getAllRegionProjects() {
        return regionProjectRepository.findAll().stream()
            .map(rp -> {
                return RegionProjectsDTO.builder()
                    .regionCode(rp.getRegionCode())
                    .regionName(rp.getRegionName())
                    .currencyCode(rp.getCurrencyCode())
                    .currencyName(rp.getCurrencyName())
                    .projectId(rp.getProjectId())
                    .projectName(rp.getProjectName())
                    .projectAmount(rp.getProjectAmount())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取所有默认地区参数
     *
     * @return RegionCurrencyDTO
     */
    @Override
    @Transactional(readOnly = true)
    public List<RegionCurrencyDTO> getAllRegionCurrencies() {
        return regionCurrencyRepository.findAll().stream()
            .map(rc -> {
                return RegionCurrencyDTO.builder()
                    .regionCode(rc.getRegionCode())
                    .regionName(rc.getRegionName())
                    .currencyCode(rc.getCurrencyCode())
                    .currencyName(rc.getCurrencyName())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * 更新角色权限
     */
    @Override
    @Transactional
    public UpdateRolePermissionDTO updateRolePermission(UpdateRolePermissionDTO updateDTO) {
        // 查找对应的 RolePermission
        RolePermission rolePermission = rolePermissionRepository.findByIdRoleIdAndIdPermissionId(updateDTO.getRoleId(), updateDTO.getPermissionId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("RolePermission not found for roleId: " + updateDTO.getRoleId()
                        + " and permissionId: " + updateDTO.getPermissionId()));
        // 更新 rate 和 isEnabled
        if (updateDTO.getRate1() != null) {
            rolePermission.setRate1(updateDTO.getRate1());
        }

        if (updateDTO.getRate2() != null) {
            rolePermission.setRate2(updateDTO.getRate2());
        }

        if (updateDTO.getIsEnabled() != null) {
            rolePermission.setIsEnabled(updateDTO.getIsEnabled());
        }

        return UpdateRolePermissionDTO.builder()
                .roleId(rolePermission.getId().getRoleId())
                .permissionId(rolePermission.getId().getPermissionId())
                .rate1(rolePermission.getRate1())
                .rate2(rolePermission.getRate2())
                .isEnabled(rolePermission.getIsEnabled())
                .build();
    }

    /**
     * 更新项目
     */
    @Override
    @Transactional
    public UpdateProjectDTO updateProject(UpdateProjectDTO updateProjectDTO) {
        // 查找对应的 Project
        Project project = projectRepository.findByProjectId(updateProjectDTO.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found for projectId: " + updateProjectDTO.getProjectId()));

        // 更新项目属性
        project.setProjectAmount(updateProjectDTO.getProjectAmount());

        return UpdateProjectDTO.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .projectAmount(project.getProjectAmount())
                .build();
    }

    /**
     * 更新区域
     */
    @Override
    @Transactional
    public UpdateRegionDTO updateRegion(UpdateRegionDTO regionDTO) {

        Region region = null;

        if (regionDTO.getRegionId() != null) {
            region = regionRepository.findByRegionId(regionDTO.getRegionId()).orElse(null);
        }

        if (region == null) {
            region = Region.builder()
                    .regionCode(regionDTO.getRegionCode())
                    .regionName(regionDTO.getRegionName())
                    .currencyName(regionDTO.getCurrencyName())
                    .build();

            Region savedRegion = regionRepository.save(region);
            
            region.setRegionProjects(regionDTO.getProjects().stream()
                    .map(projectDTO -> RegionProject.builder()
                            .id(RegionProject.RegionProjectId.builder()
                                    .regionId(savedRegion.getRegionId())
                                    .projectId(projectDTO.getProjectId())
                                    .build())
                            .region(savedRegion)
                            .projectName(projectDTO.getProjectName())
                            .projectAmount(projectDTO.getProjectAmount())
                            .build())
                    .collect(Collectors.toList()));
        } else {
            List<RegionProject> existingProjects = region.getRegionProjects();

            Map<Integer, RegionProject> existingProjectMap = existingProjects.stream()
                    .collect(Collectors.toMap(rp -> rp.getId().getProjectId(), Function.identity()));

            for (UpdateProjectDTO dto : regionDTO.getProjects()) {
                Integer projectId = dto.getProjectId();
                RegionProject existingProject = existingProjectMap.get(projectId);

                if (existingProject != null) {
                    existingProject.setProjectName(dto.getProjectName());
                    existingProject.setProjectAmount(dto.getProjectAmount());
                }
            }

            Region savedRegion = regionRepository.findByRegionId(regionDTO.getRegionId()).orElse(null);
            savedRegion.setRegionCode(regionDTO.getRegionCode());
            savedRegion.setRegionName(regionDTO.getRegionName());
            savedRegion.setCurrency(regionDTO.getCurrency());
            
        }

        return mapToUpdateRegionDTO(region);
    }

    /**
     * 将 Region 实体转换为 UpdateRegionDTO
     */
    private UpdateRegionDTO mapToUpdateRegionDTO(Region region) {
        return UpdateRegionDTO.builder()
                .regionId(region.getRegionId())
                .regionCode(region.getRegionCode())
                .regionName(region.getRegionName())
                .currencyName(region.getCurrencyName())
                .projects(region.getRegionProjects().stream()
                        .map(rp -> UpdateProjectDTO.builder()
                                .projectId(rp.getId().getProjectId())
                                .projectName(rp.getProjectName())
                                .projectAmount(rp.getProjectAmount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 删除区域
     *
     * @param regionId 区域ID
     */
    @Override
    @Transactional
    public Boolean deleteRegion(Integer regionId) {
        // 查找对应的区域
        Region region = regionRepository.findByRegionId(regionId)
            .orElseThrow(() -> new IllegalArgumentException("Region not found for regionId: " + regionId));

        // 删除区域
        regionRepository.delete(region);
        return true;
    }

    /**
     * 搜索用户权限
     *
     * @param filter   过滤条件
     * @param pageable 分页信息
     * @return 分页的用户权限响应
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SearchUserPermissionsResponseDTO> searchUserPermissions(SearchUserPermissionDTO filter, Pageable pageable) {
        Specification<PermissionRelationship> spec = (root, query, criteriaBuilder) -> {
            query.distinct(true); // 确保结果不重复

            // 使用 join 预加载 BackendUser
            Join<PermissionRelationship, BackendUser> userJoin = root.join("user", JoinType.INNER);
            
            // 左连接 RoleRelationship
            Join<BackendUser, RoleRelationship> roleJoin = userJoin.join("roleRelationships", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            // 根据 roleId 过滤
            if (filter.getRoleId() != null) {
                predicates.add(criteriaBuilder.equal(roleJoin.get("roleId"), filter.getRoleId()));
            }

            // 根据 userId 过滤
            if (filter.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(userJoin.get("userId"), filter.getUserId()));
            }

            // 根据 username 过滤
            if (StringUtils.hasText(filter.getUsername())) {
                predicates.add(criteriaBuilder.like(userJoin.get("username"), "%" + filter.getUsername().trim() + "%"));
            }

            // 根据 fullname 过滤
            if (StringUtils.hasText(filter.getFullname())) {
                predicates.add(criteriaBuilder.like(userJoin.get("fullname"), "%" + filter.getFullname().trim() + "%"));
            }

            // 根据 permissionId 过滤
            if (filter.getPermissionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("permissionId"), filter.getPermissionId()));
            }

            // 根据 isEnabled 过滤
            if (filter.getIsEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getIsEnabled()));
            }

            // 处理 isCurrent 过滤
            if (Boolean.TRUE.equals(filter.getIsCurrent())) {
                LocalDate queryDate = LocalDate.now();
    
                // 过滤角色关系的日期
                Predicate roleStartDatePredicate = criteriaBuilder.lessThanOrEqualTo(roleJoin.get("startDate"), queryDate);
                Predicate roleEndDatePredicate = criteriaBuilder.or(
                    criteriaBuilder.greaterThan(roleJoin.get("endDate"), queryDate),
                    criteriaBuilder.equal(roleJoin.get("endDate"), queryDate),
                    criteriaBuilder.isNull(roleJoin.get("endDate"))
                );
    
                // 过滤权限关系的日期
                Predicate permStartDatePredicate = criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), queryDate);
                Predicate permEndDatePredicate = criteriaBuilder.or(
                    criteriaBuilder.greaterThan(root.get("endDate"), queryDate),
                    criteriaBuilder.equal(root.get("endDate"), queryDate),
                    criteriaBuilder.isNull(root.get("endDate"))
                );
    
                // 将角色和权限的日期条件结合起来
                Predicate isCurrentPredicate = criteriaBuilder.and(
                    roleStartDatePredicate,
                    roleEndDatePredicate,
                    permStartDatePredicate,
                    permEndDatePredicate
                );
    
                predicates.add(isCurrentPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<PermissionRelationship> results = permissionRelationshipRepository.findAll(spec, pageable);

        return results.map(entity -> {
            BackendUser user = entity.getUser();

            return SearchUserPermissionsResponseDTO.builder()
                    .id(entity.getRecordId())
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullname(user.getFullname())
                    .roleId(user.getRole().getRole().getRoleId())
                    .permissionId(entity.getPermissionId())
                    .rate1(entity.getRate1())
                    .rate2(entity.getRate2())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .isEnabled(entity.getStatus())
                    .build();
        });
    }

    /**
     * 更新用户权限
     *
     * @param updateDTO 更新信息
     * @return 更新后的信息
     */
    @Override
    @Transactional
    public UpdateUserPermissionDTO updateUserPermission(UpdateUserPermissionDTO updateDTO) {
        PermissionRelationship permissionRelationship = permissionRelationshipRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("PermissionRelationship not found for id: " + updateDTO.getId()));

        // 更新 rate1
        if (updateDTO.getRate1() != null) {
            permissionRelationship.setRate1(updateDTO.getRate1());
        }

        // 更新 rate2
        if (updateDTO.getRate2() != null) {
            permissionRelationship.setRate2(updateDTO.getRate2());
        }

        // 更新开始日期
        if (updateDTO.getStartDate() != null) {
            permissionRelationship.setStartDate(updateDTO.getStartDate());
        }

        // 更新结束日期
        if (updateDTO.getEndDate() != null) {
            // 验证 startDate <= endDate
            if (permissionRelationship.getStartDate() != null && updateDTO.getEndDate().isBefore(permissionRelationship.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date.");
            }
            permissionRelationship.setEndDate(updateDTO.getEndDate());
        }

        // 更新状态
        if (updateDTO.getIsEnabled() != null) {
            permissionRelationship.setStatus(updateDTO.getIsEnabled());
        }

        // 保存更新
        PermissionRelationship updatedPermission = permissionRelationshipRepository.save(permissionRelationship);

        return UpdateUserPermissionDTO.builder()
                .id(updatedPermission.getRecordId())
                .rate1(updatedPermission.getRate1())
                .rate2(updatedPermission.getRate2())
                .startDate(updatedPermission.getStartDate())
                .endDate(updatedPermission.getEndDate())
                .isEnabled(updatedPermission.getStatus())
                .build();
    }
}
