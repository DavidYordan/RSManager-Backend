package com.rsmanager.service;

import java.util.Optional;

import com.rsmanager.model.BackendRole;

public interface BackendRoleService {

    /**
     * 根据roleId获取角色
     *
     * @param roleId 角色ID
     * @return BackendRole
     */
    Optional<BackendRole> findByRoleId(Integer roleId);
    
    /**
     * 根据角色名称获取角色
     *
     * @param name 角色名称
     * @return BackendRole
     */
    Optional<BackendRole> findByName(String name);
}
