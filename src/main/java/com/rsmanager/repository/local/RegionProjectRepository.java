package com.rsmanager.repository.local;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.rsmanager.dto.system.RegionProjectsDTO;
import com.rsmanager.model.RegionProject;

import org.springframework.stereotype.Repository;

@Repository
public interface RegionProjectRepository extends JpaRepository<RegionProject, RegionProject.RegionProjectId>, JpaSpecificationExecutor<RegionProject> {
    
    @Query("""
            SELECT new com.rsmanager.dto.system.RegionProjectsDTO(
                r.id.regionCode,
                r.regionName,
                r.id.currencyCode,
                r.currencyName,
                p.roleId,
                r.id.projectId,
                r.projectName,
                r.projectAmount
            )
            FROM RegionProject r
            LEFT JOIN r.project p
        """)
    List<RegionProjectsDTO> getAllRegionProjects();
}
