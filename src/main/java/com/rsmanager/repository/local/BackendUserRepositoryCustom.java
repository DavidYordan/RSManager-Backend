package com.rsmanager.repository.local;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rsmanager.dto.traffic.SearchTrafficDTO;
import com.rsmanager.dto.traffic.SearchTrafficResponseDTO;
import com.rsmanager.dto.user.SearchRolePermissionsDTO;
import com.rsmanager.dto.user.SearchRolePermissionsResponseDTO;
import com.rsmanager.dto.user.SearchUsersDTO;
import com.rsmanager.dto.user.SearchUsersResponseDTO;

public interface BackendUserRepositoryCustom {

    Page<SearchRolePermissionsResponseDTO> searchRolePermissions(SearchRolePermissionsDTO request, Pageable pageable);

    Page<SearchTrafficResponseDTO> searchTraffics(SearchTrafficDTO request, Pageable pageable);

    Page<SearchUsersResponseDTO> searchUsers(SearchUsersDTO request, Pageable pageable);
}
