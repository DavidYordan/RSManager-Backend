package com.rsmanager.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.RoleRelationship;

@Repository
public interface RoleRelationshipRepository extends JpaRepository<RoleRelationship, Long> {

}
