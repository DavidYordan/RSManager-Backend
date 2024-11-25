package com.rsmanager.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rsmanager.model.BackendUser;

import java.util.Collection;

@Getter
@Builder
public class CustomUserDetails implements UserDetails {
    private final BackendUser operator;
    private final String username;
    private final String password;
    private final Integer roleId;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return operator.getStatus();
    }
}
