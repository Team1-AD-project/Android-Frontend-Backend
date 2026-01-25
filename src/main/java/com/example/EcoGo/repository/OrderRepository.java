package com.example.EcoGo.repository;

import com.example.EcoGo.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
    // 可添加自定义查询方法
}
