package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.rsmanager.model.RolePermissionRelationship;

public interface RolePermissionRelationshipRepository extends JpaRepository<RolePermissionRelationship, Long>, JpaSpecificationExecutor<RolePermissionRelationship> {
    
}
