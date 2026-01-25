package com.example.EcoGo.dto;

public class UserResponseDto {
    private String username;
    private String email;
    private String userid;
    private String nickname;
    private String phone;

    // No-argument constructor
    public UserResponseDto() {
    }

    // Full constructor (optional, but good practice)
    public UserResponseDto(String username, String email, String userid, String nickname, String phone) {
        this.username = username;
        this.email = email;
        this.userid = userid;
        this.nickname = nickname;
        this.phone = phone;
    }

    // Getters and Setters

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

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
