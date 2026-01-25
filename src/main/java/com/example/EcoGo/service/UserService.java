package com.example.EcoGo.service;

import com.example.EcoGo.model.User;

public interface UserService {

    // 用户注册
    User registerUser(User user);

    // 用户登录
    User loginUser(User user);

    // 更新用户信息
    User updateUserProfile(User user);
}