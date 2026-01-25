package com.example.EcoGo.service;

import com.example.EcoGo.model.User;
import com.example.EcoGo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        return userRepository.save(user);  // 保存用户
    }

    @Override
    public User loginUser(User user) {
        return userRepository.findByUsername(user.getUsername());  // 根据用户名查找用户
    }

    @Override
    public User updateUserProfile(User user) {
        return userRepository.save(user);  // 更新用户信息
    }
}