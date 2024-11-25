package com.rsmanager.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import com.rsmanager.dto.system.*;
import com.rsmanager.model.BackendUser;
import com.rsmanager.model.InviterRelationship;
import com.rsmanager.model.ManagerRelationship;
import com.rsmanager.model.TbUser;
import com.rsmanager.model.Project;
import com.rsmanager.model.RegionProject;
import com.rsmanager.model.RolePermissionRelationship;
import com.rsmanager.repository.local.ProjectRepository;
import com.rsmanager.repository.local.RegionCurrencyRepository;
import com.rsmanager.repository.local.RegionProjectRepository;
import com.rsmanager.repository.local.RolePermissionRelationshipRepository;
import com.rsmanager.service.SystemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemServerImpl implements SystemService {

    private final ProjectRepository projectRepository;
    private final RegionProjectRepository regionProjectRepository;
    private final RegionCurrencyRepository regionCurrencyRepository;
    private final RolePermissionRelationshipRepository rolePermissionRelationshipRepository;

    @Override
    @Transactional(readOnly = true)
    public GlobalParamsReponseDTO getAllGlobalParams() {

        // 构建 projectDTO 列表
        List<ProjectDTO> projectDTOs = getAllProjects();

        // 构建 regionCurrencyDTO 列表
        List<RegionCurrencyDTO> regionCurrencyDTOs = getAllRegionCurrencies();

        // 构建 regionProjectsDTO 列表
        List<RegionProjectsDTO> regionProjectsDTOs = getAllRegionProjects();

        // 构建最终的 GlobalParamsReponseDTO
        GlobalParamsReponseDTO responseDTO = GlobalParamsReponseDTO.builder()
            .regionCurrencyDTOs(regionCurrencyDTOs)
            .regionProjectsDTOs(regionProjectsDTOs)
            .projectDTOs(projectDTOs)
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
                    .roleId(p.getRoleId())
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
                    .regionCode(rp.getId().getRegionCode())
                    .regionName(rp.getRegionName())
                    .currencyCode(rp.getId().getCurrencyCode())
                    .currencyName(rp.getCurrencyName())
                    .projectId(rp.getId().getProjectId())
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
     * 更新默认项目
     */
    @Override
    @Transactional
    public Boolean updateProject(UpdateProjectDTO updateProjectDTO) {

        Project project = projectRepository.findById(updateProjectDTO.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("RegionProject not found for projectId: " + updateProjectDTO.getProjectId()));
        
        project.setProjectName(updateProjectDTO.getProjectName());
        project.setProjectAmount(updateProjectDTO.getProjectAmount());

        projectRepository.save(project);

        return true;
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
     * 搜索用户权限
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SearchRolePermissionRelationshipResponseDTO> searchRolePermissionRelationships(SearchRolePermissionRelationshipDTO request) {
        
        Pageable pageable =  PageRequest.of(request.getPage(), request.getSize(), Sort.by("userId").ascending());
        
        Specification<RolePermissionRelationship> spec = (root, query, criteriaBuilder) -> {

            // 使用 join 预加载 BackendUser
            Join<RolePermissionRelationship, BackendUser> userJoin = root.join("user", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            if (request.getRoleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("roleId"), request.getRoleId()));
            }
            if (StringUtils.hasText(request.getRoleName())) {
                predicates.add(criteriaBuilder.like(root.get("roleName"), "%" + request.getRoleName() + "%"));
            }
            if (request.getPermissionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("permissionId"), request.getPermissionId()));
            }
            if (StringUtils.hasText(request.getPermissionName())) {
                predicates.add(criteriaBuilder.like(root.get("permissionName"), "%" + request.getPermissionName() + "%"));
            }
            if (request.getIsEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isEnabled"), request.getIsEnabled()));
            }
            if (request.getIsCurrent() != null) {
                if (request.getIsCurrent()) {
                    predicates.add(criteriaBuilder.isNull(root.get("endDate")));
                } else {
                    predicates.add(criteriaBuilder.isNotNull(root.get("endDate")));
                }
            }
            if (request.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(userJoin.get("userId"), request.getUserId()));
            }
            if (StringUtils.hasText(request.getUsername())) {
                predicates.add(criteriaBuilder.like(userJoin.get("username"), "%" + request.getUsername() + "%"));
            }
            if (StringUtils.hasText(request.getFullname())) {
                predicates.add(criteriaBuilder.like(userJoin.get("fullname"), "%" + request.getFullname() + "%"));
            }
            if (request.getManagerId() != null || StringUtils.hasText(request.getManagerName()) || StringUtils.hasText(request.getManagerFullname())) {
                Join<BackendUser, ManagerRelationship> managerJoin = userJoin.join("manager", JoinType.LEFT);
                if (request.getManagerId() != null) {
                    predicates.add(criteriaBuilder.equal(managerJoin.get("userId"), request.getManagerId()));
                }
                if (StringUtils.hasText(request.getManagerName())) {
                    predicates.add(criteriaBuilder.like(managerJoin.get("username"), "%" + request.getManagerName() + "%"));
                }
                if (StringUtils.hasText(request.getManagerFullname())) {
                    predicates.add(criteriaBuilder.like(managerJoin.get("fullname"), "%" + request.getManagerFullname() + "%"));
                }
            }
            if (request.getInviterId() != null || StringUtils.hasText(request.getInviterName()) || StringUtils.hasText(request.getInviterFullname())) {
                Join<BackendUser, InviterRelationship> inviterJoin = userJoin.join("inviter", JoinType.LEFT);
                if (request.getInviterId() != null) {
                    predicates.add(criteriaBuilder.equal(inviterJoin.get("userId"), request.getInviterId()));
                }
                if (StringUtils.hasText(request.getInviterName())) {
                    predicates.add(criteriaBuilder.like(inviterJoin.get("username"), "%" + request.getInviterName() + "%"));
                }
                if (StringUtils.hasText(request.getInviterFullname())) {
                    predicates.add(criteriaBuilder.like(inviterJoin.get("fullname"), "%" + request.getInviterFullname() + "%"));
                }
            }
            if (StringUtils.hasText(request.getInvitationCode()) || StringUtils.hasText(request.getInviterCode())) {
                Join<BackendUser, TbUser> tbUserJoin = userJoin.join("tbUser", JoinType.LEFT);
                if (StringUtils.hasText(request.getInvitationCode())) {
                    predicates.add(criteriaBuilder.equal(tbUserJoin.get("invitationCode"), request.getInvitationCode()));
                }
                if (StringUtils.hasText(request.getInviterCode())) {
                    predicates.add(criteriaBuilder.equal(tbUserJoin.get("inviterCode"), request.getInviterCode()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<RolePermissionRelationship> resultPage = rolePermissionRelationshipRepository.findAll(spec, pageable);

        List<RolePermissionRelationship> content = resultPage.getContent();

        List<SearchRolePermissionRelationshipResponseDTO> responseDTOs = content.stream()
            .map(r -> {
                return SearchRolePermissionRelationshipResponseDTO.builder()
                    .recordId(r.getRecordId())
                    .userId(r.getUser().getUserId())
                    .username(r.getUser().getUsername())
                    .fullname(r.getUser().getFullname())
                    .roleId(r.getRoleId())
                    .roleName(r.getRoleName())
                    .permissionId(r.getPermissionId())
                    .permissionName(r.getPermissionName())
                    .rate1(r.getRate1())
                    .rate2(r.getRate2())
                    .startDate(r.getStartDate())
                    .endDate(r.getEndDate())
                    .status(r.getStatus())
                    .build();
            })
            .collect(Collectors.toList());

        return new PageImpl<>(responseDTOs, pageable, resultPage.getTotalElements());
    }

    /**
     * 更新用户权限
     */
    @Override
    @Transactional
    public Boolean updateUserPermissionRelationship(UpdateRolePermissionRelationshipDTO updateDTO) {
        
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
