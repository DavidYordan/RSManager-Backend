package com.rsmanager.service;

import jakarta.servlet.http.HttpSession;

public interface CaptchaService {
    
    /**
     * 生成验证码文本
     */
    public String generateCaptchaText();

    /**
     * 生成验证码图片的字节数组
     */
    public byte[] generateCaptchaImageBytes(String captchaText);

    /**
     * 验证验证码是否正确
     */
    public boolean validateCaptcha(String inputCaptcha, HttpSession session);
}
