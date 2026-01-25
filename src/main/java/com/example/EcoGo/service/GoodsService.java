package com.example.EcoGo.service;

import com.example.EcoGo.model.Goods;
import java.util.List;

public interface GoodsService {

    // 创建商品
    Goods createGoods(Goods goods);

    // 更新商品
    Goods updateGoods(String id, Goods goods);

    // 删除商品
    void deleteGoods(String id);

    // 获取所有商品
    List<Goods> getAllGoods();

    // 删除所有商品
    void deleteAllGoods();

    // 插入测试商品数据
    void insertTestData();
}