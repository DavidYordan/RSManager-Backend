package com.rsmanager.service.impl;

import com.google.code.kaptcha.Producer;
import com.rsmanager.service.CaptchaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service("captchaService")
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private Producer captchaProducer;

    /**
     * 生成验证码文本
     */
    public String generateCaptchaText() {
        return captchaProducer.createText();
    }

    /**
     * 生成验证码图片的字节数组
     */
    public byte[] generateCaptchaImageBytes(String text) {
        BufferedImage image = captchaProducer.createImage(text);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            // 处理异常
            // 你可以记录日志或抛出自定义异常
        }
        return baos.toByteArray();
    }

    /**
     * 验证验证码是否正确
     */
    public boolean validateCaptcha(String inputCaptcha, HttpSession session) {
        String sessionCaptcha = (String) session.getAttribute("captchaCode");
        if (sessionCaptcha == null) {
            return false;
        }
        return sessionCaptcha.equalsIgnoreCase(inputCaptcha);
    }
}
