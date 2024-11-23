package com.rsmanager.service.impl;

import com.rsmanager.dto.login.LoginRequest;
import com.rsmanager.dto.login.LoginResponse;
import com.rsmanager.exception.auth.BadCaptchaException;
import com.rsmanager.exception.auth.InvalidCredentialsException;
import com.rsmanager.exception.auth.UserDisabledException;
import com.rsmanager.security.CustomUserDetails;
import com.rsmanager.security.JwtTokenProvider;
import com.rsmanager.service.AuthService;
import com.rsmanager.service.CaptchaService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CaptchaService captchaService;

    @Override
    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest, HttpSession session) {
        // 校验验证码
        boolean isCaptchaValid = captchaService.validateCaptcha(loginRequest.getCaptchaCode(), session);
        if (!isCaptchaValid) {
            logger.warn("Captcha validation failed for user: {}", loginRequest.getUsername());
            throw new BadCaptchaException("Invalid captcha");
        }

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 设置认证信息到 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成 JWT 令牌
            String jwt = tokenProvider.generateToken(authentication);

            // 获取用户信息
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 检查status
            if (!userDetails.isEnabled()) {
                logger.warn("User '{}' is disabled", userDetails.getUsername());
                throw new UserDisabledException("User is disabled");
            }

            Integer roleId = userDetails.getRoleId();
            if (roleId == null) {
                logger.warn("User '{}' has no role assigned", userDetails.getUsername());
                throw new UserDisabledException("User has no role assigned");
            }
            if (roleId == 6) {
                logger.warn("User '{}' is not allowed to log in", userDetails.getUsername());
                throw new UserDisabledException("User is not allowed to log in");
            }

            // 构建响应
            LoginResponse response = LoginResponse.builder()
                .token(jwt)
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .roleId(roleId)
                .build();

            return response;

        } catch (BadCredentialsException ex) {
            // 捕获用户名或密码错误的异常，返回自定义错误信息
            logger.warn("Authentication failed for user: {}", loginRequest.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        } catch (Exception ex) {
            // 捕获其他认证异常
            logger.error("Authentication error for user: {}", loginRequest.getUsername(), ex);
            throw new AuthenticationServiceException("Authentication failed");
        }
    }

    @Override
    public byte[] getCaptcha(HttpSession session) {
        // 生成验证码文本
        String captchaText = captchaService.generateCaptchaText();

        // 将验证码文本存储在会话中
        session.setAttribute("captchaCode", captchaText);

        // 生成验证码图片的字节数组
        return captchaService.generateCaptchaImageBytes(captchaText);
    }

    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public Long getCurrentUserId() {
        // 获取当前用户的ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        return null;
    }

    @Override
    public String getCurrentUsername() {
        // 获取当前用户的用户名
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }

    @Override
    public String getCurrentFullname() {
        // 获取当前用户的全名
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getFullname();
        }
        return null;
    }

    @Override
    public Integer getCurrentUserRoleId() {
        // 获取当前用户的角色ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getRoleId();
        }
        return null;
    }

    /**
    * 检查当前用户的 roleId 是否在指定的角色组中
    */
    public boolean hasRoleIn(Integer... roleIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Integer currentUserRoleId = userDetails.getRoleId();
            for (Integer roleId : roleIds) {
                if (roleId.equals(currentUserRoleId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
