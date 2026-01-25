package com.example.EcoGo.controller;

import com.example.EcoGo.model.Goods;
import com.example.EcoGo.service.GoodsService;  // 引用接口类，而不是实现类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;  // 注入接口类型，Spring 会自动注入实现类

    // 获取所有商品
    @GetMapping
    public List<Goods> getAllGoods() {
        return goodsService.getAllGoods();
    }

    // 创建商品
    @PostMapping
    public Goods createGoods(@RequestBody Goods goods) {
        return goodsService.createGoods(goods);
    }

    // 删除所有商品
    @DeleteMapping("/deleteAll")
    public void deleteAllGoods() {
        goodsService.deleteAllGoods();
    }

    // 更新商品
    @PutMapping("/{id}")
    public Goods updateGoods(@PathVariable String id, @RequestBody Goods goods) {
        return goodsService.updateGoods(id, goods);
    }

    // 删除单个商品
    @DeleteMapping("/{id}")
    public void deleteGoods(@PathVariable String id) {
        goodsService.deleteGoods(id);
    }

    // 插入测试商品数据
    @PostMapping("/test")
    public void insertTestData() {
        goodsService.insertTestData();  // 调用 Service 插入测试数据
    }
}