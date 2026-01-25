package com.example.EcoGo.dto;

public class UserResponseDto {

    private String username;
    private String email;

    // 构造方法
    public UserResponseDto(String username) {
        this.username = username;
    }

    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}