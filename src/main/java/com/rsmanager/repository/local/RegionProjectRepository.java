package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.rsmanager.model.RegionProject;

public interface RegionProjectRepository extends JpaRepository<RegionProject, Integer>, JpaSpecificationExecutor<RegionProject> {
    
}
