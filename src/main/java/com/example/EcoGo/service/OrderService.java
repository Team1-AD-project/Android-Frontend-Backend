package com.example.EcoGo.service;

import com.example.EcoGo.model.Order;
import java.util.List;

public interface OrderService {

    // 创建订单
    Order createOrder(Order order);

    // 更新订单
    Order updateOrder(String id, Order order);

    // 删除订单
    void deleteOrder(String id);

    // 获取所有订单
    List<Order> getAllOrders();
}