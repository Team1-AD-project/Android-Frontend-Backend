package com.example.EcoGo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 配置请求权限控制
        http
            .authorizeHttpRequests()  // 使用新的方法
            .requestMatchers("/**").permitAll()  // 允许所有请求
            .and()
            .csrf().disable();  // 禁用 CSRF 防护

        return http.build();
    }
}
