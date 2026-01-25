package com.example.EcoGo.repository;

import com.example.EcoGo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

// 扩展 MongoRepository，自动提供基本的 CRUD 操作
public interface UserRepository extends MongoRepository<User, String> {

    // 根据用户名查找用户
    User findByUsername(String username);
}