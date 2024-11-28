package com.rsmanager.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.rsmanager.model.BackendUser;

@Component
public class UserContext {

    /**
     * 获取当前认证信息
     */
    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户的 CustomUserDetails
     */
    private CustomUserDetails getUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return (CustomUserDetails) principal;
            }
        }
        return null;
    }

    /**
     * 获取当前用户
     */
    public BackendUser getOperator() {
        CustomUserDetails userDetails = getUserDetails();
        return userDetails != null ? userDetails.getOperator() : null;
    }

    /**
     * 获取当前用户的 ID
     */
    public Long getOperatorId() {
        CustomUserDetails userDetails = getUserDetails();
        return userDetails != null ? userDetails.getOperator().getUserId() : null;
    }

    /**
     * 获取当前用户的用户名
     */
    public String getUsername() {
        CustomUserDetails userDetails = getUserDetails();
        return userDetails != null ? userDetails.getUsername() : null;
    }

    /**
     * 获取当前用户的全名
     */
    public String getFullname() {
        CustomUserDetails userDetails = getUserDetails();
        return userDetails != null ? userDetails.getOperator().getFullname() : null;
    }

    /**
     * 获取当前用户的角色 ID
     */
    public Integer getRoleId() {
        CustomUserDetails userDetails = getUserDetails();
        return userDetails != null ? userDetails.getRoleId() : null;
    }

    /**
     * 检查当前用户是否拥有某些角色
     */
    public boolean hasRole(Integer... roleIds) {
        Integer roleId = getRoleId();
        if (roleId != null) {
            for (Integer allowedRole : roleIds) {
                if (roleId.equals(allowedRole)) {
                    return true;
                }
            }
        }
        return false;
    }
}
