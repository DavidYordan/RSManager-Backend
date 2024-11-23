package com.rsmanager.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rsmanager.repository.local.BackendRoleRepository;
import com.rsmanager.service.BackendRoleService;
import com.rsmanager.model.BackendRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BackendRoleServiceImpl implements BackendRoleService {

    private final BackendRoleRepository backendRoleRepository;

    @Override
    public Optional<BackendRole> findByRoleId(Integer roleId) {
        return backendRoleRepository.findById(roleId);
    }

    @Override
    public Optional<BackendRole> findByName(String name) {
        return backendRoleRepository.findByRoleName(name);
    }
    
}
