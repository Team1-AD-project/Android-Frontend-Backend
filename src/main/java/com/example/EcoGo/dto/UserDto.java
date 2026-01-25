package com.example.EcoGo.dto;

public class UserDto {

    private String phone;
    private String password;
    private String nickname;
    private String avatar;

    // Constructors, getters, and setters
    public UserDto() {}

    public UserDto(String phone, String password, String nickname, String avatar) {
        this.phone = phone;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}