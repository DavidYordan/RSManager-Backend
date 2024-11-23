package com.rsmanager.repository.local;

import com.rsmanager.model.PermissionRelationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRelationshipRepository extends JpaRepository<PermissionRelationship, Long>, JpaSpecificationExecutor<PermissionRelationship> {
    
}