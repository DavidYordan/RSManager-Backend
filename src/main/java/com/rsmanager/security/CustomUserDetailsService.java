package com.rsmanager.security;

import com.rsmanager.model.BackendUser;
import com.rsmanager.repository.local.BackendUserRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

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
        BackendUser user = backendUserRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("No user found with username '{}'", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        // 把roleName转换为GrantedAuthority
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(user.getRole().getRole().getRoleName()));

        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getFullname(),
                user.getPassword(),
                user.getRegionName(),
                user.getCurrency(),
                user.getRole().getRole().getRoleId(),
                authorities,
                user.getStatus()
        );
    }
}