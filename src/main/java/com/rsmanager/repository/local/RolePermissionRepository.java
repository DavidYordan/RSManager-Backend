package com.rsmanager.repository.local;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rsmanager.model.RolePermission;
import com.rsmanager.model.RolePermission.RolePermissionId;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId>, JpaSpecificationExecutor<RolePermission> {
    // 通过roleid和permissionid查找
    Optional<RolePermission> findByIdRoleIdAndIdPermissionId(Integer roleId, Integer permissionId);
    List<RolePermission> findByIdRoleId(Integer roleId);
}