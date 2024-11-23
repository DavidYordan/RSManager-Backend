package com.rsmanager.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final String username;
    private final String fullname;
    private final String password;
    private final String regionName;
    private final String currency;
    private final Integer roleId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isEnabled;

    // 新增构造函数
    public CustomUserDetails(Long userId, String username, String fullname, String password, String regionName,
                    String currency, Integer roleId, Collection<? extends GrantedAuthority> authorities,
                    boolean isEnabled) {
        this.userId = userId;
        this.username = username;
        this.fullname = fullname;
        this.password = password;
        this.regionName = regionName;
        this.currency = currency;
        this.roleId = roleId;
        this.authorities = authorities;
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 根据需求调整
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 根据需求调整
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 根据需求调整
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
