package com.rsmanager.security;

import com.rsmanager.model.BackendUser;
import com.rsmanager.model.RolePermissionRelationship;
import com.rsmanager.repository.local.BackendUserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

@Service("customUserDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final BackendUserRepository backendUserRepository;

    /**
     * 根据用户名加载用户信息
     *
     * @param username 用户名
     * @return UserDetails
     * @throws UsernameNotFoundException 如果用户未找到
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BackendUser operator = backendUserRepository.findByUsername(username)
                .filter(BackendUser::getStatus)
                .orElseThrow(() -> {
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        RolePermissionRelationship rolePRelationship = operator.getRolePermissionRelationships().stream()
                .filter(rp -> rp.getEndDate() == null)
                .findFirst()
                .orElseThrow(() -> {
                    return new UsernameNotFoundException(username + "not found role");
                });

        // 把roleName转换为GrantedAuthority
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(rolePRelationship.getRoleName()));

        return new CustomUserDetails(
                operator,
                operator.getUsername(),
                operator.getPassword(),
                rolePRelationship.getRoleId(),
                authorities
        );
    }
}