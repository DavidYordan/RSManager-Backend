package com.rsmanager.service;

import com.rsmanager.dto.login.LoginRequest;
import com.rsmanager.dto.login.LoginResponse;

import jakarta.servlet.http.HttpSession;

public interface AuthService {

    /**
     * 认证用户
     * @param loginRequest 登录请求
     * @param session HttpSession
     * @return 登录响应
     */
    LoginResponse authenticateUser(LoginRequest loginRequest, HttpSession session);

    /**
     * 获取验证码
     * @param session HttpSession
     * @return 验证码字节数组
     */
    byte[] getCaptcha(HttpSession session);

    /**
     * 登出
     * @param session HttpSession
     */
    void logout(HttpSession session);

    /**
     * 获取当前用户ID
     * @return 当前用户ID
     */
    Long getCurrentUserId();

    /**
     * 获取当前用户名
     * @return 当前用户名
     */
    String getCurrentUsername();

    /**
     * 获取当前用户全名
     * @return 当前用户全名
     */
    String getCurrentFullname();

    /**
     * 获取当前用户角色ID
     * @return 当前用户角色ID
     */
    Integer getCurrentUserRoleId();
}
