package com.example.EcoGo.service;

import com.example.EcoGo.model.Order;
import com.example.EcoGo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Order createOrder(Order order) {
        return orderRepository.save(order);  // 创建订单并保存
    }

    @Override
    public Order updateOrder(String id, Order order) {
        return orderRepository.save(order);  // 更新订单
    }

    @Override
    public void deleteOrder(String id) {
        orderRepository.deleteById(id);  // 删除订单
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();  // 获取所有订单
    }
}