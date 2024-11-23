package com.rsmanager.repository.local;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.BackendRole;

@Repository
public interface BackendRoleRepository extends JpaRepository<BackendRole, Integer>, JpaSpecificationExecutor<BackendRole> {
    
    Optional<BackendRole> findByRoleName(String roleName);
}
