package com.rsmanager.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

    @Bean
    public DefaultKaptcha captchaProducer() {
        Properties properties = new Properties();
        // 设置图片宽度
        properties.setProperty("kaptcha.image.width", "150");
        // 设置图片高度
        properties.setProperty("kaptcha.image.height", "50");
        // 设置验证码文本长度为4
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 设置验证码文本字符集为大写字母
        properties.setProperty("kaptcha.textproducer.char.string", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 设置字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "40");
        // 设置背景颜色为白色
        properties.setProperty("kaptcha.background.clear.from", "white");
        properties.setProperty("kaptcha.background.clear.to", "white");
        // 设置文字颜色为黑色
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        // 禁用噪点
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        // 设置边框为无
        properties.setProperty("kaptcha.border", "no");
        // 设置干扰效果为无
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");

        Config config = new Config(properties);
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
