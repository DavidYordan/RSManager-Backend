package com.rsmanager.repository.local;

import com.rsmanager.model.Project;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    // 通过 projectId 查询
    Optional<Project> findByProjectId(Integer projectId);
}   

