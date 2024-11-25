package com.rsmanager.service;

import com.rsmanager.dto.login.LoginRequest;
import com.rsmanager.dto.login.LoginResponse;
import com.rsmanager.model.BackendUser;

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
     * 获取当前用户
     * @return 当前用户
     */
    BackendUser getOperator();

    /**
     * 获取当前用户ID
     * @return 当前用户ID
     */
    Long getOperatorId();

    /**
     * 获取当前用户名
     * @return 当前用户名
     */
    String getOperatorName();

    /**
     * 获取当前用户全名
     * @return 当前用户全名
     */
    String getOperatorFullname();

    /**
     * 获取当前用户角色ID
     * @return 当前用户角色ID
     */
    Integer getOperatorRoleId();
}
