package com.example.EcoGo.controller;

import com.example.EcoGo.model.User;
import com.example.EcoGo.service.UserServiceImpl;  // 使用修改后的Impl类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;  // 使用Impl类

    // 用户注册
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // 用户登录
    @PostMapping("/login")
    public User loginUser(@RequestBody User user) {
        return userService.loginUser(user);
    }

    // 更新用户信息
    @PutMapping("/profile")
    public User updateUserProfile(@RequestBody User user) {
        return userService.updateUserProfile(user);
    }
}