package com.example.EcoGo.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {

    private final PasswordEncoder passwordEncoder;

    public PasswordUtils() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 校验密码
     * 
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 匹配结果
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
