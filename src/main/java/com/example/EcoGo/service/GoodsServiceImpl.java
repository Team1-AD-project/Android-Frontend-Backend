package com.example.EcoGo.service;

import com.example.EcoGo.model.Goods;
import com.example.EcoGo.repository.GoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsRepository goodsRepository;

    // 创建商品
    @Override
    public Goods createGoods(Goods goods) {
        return goodsRepository.save(goods);  // 使用 MongoRepository 保存数据
    }

    // 更新商品
    @Override
    public Goods updateGoods(String id, Goods goods) {
        Optional<Goods> existingGoods = goodsRepository.findById(id);
        if (existingGoods.isPresent()) {
            Goods updatedGoods = existingGoods.get();
            updatedGoods.setName(goods.getName());
            updatedGoods.setDescription(goods.getDescription());
            updatedGoods.setPrice(goods.getPrice());
            updatedGoods.setStock(goods.getStock());
            return goodsRepository.save(updatedGoods);
        } else {
            throw new RuntimeException("Product not found with id " + id);
        }
    }

    // 删除商品
    @Override
    public void deleteGoods(String id) {
        goodsRepository.deleteById(id);
    }

    // 获取所有商品
    @Override
    public List<Goods> getAllGoods() {
        return goodsRepository.findAll();
    }

    // 删除所有商品
    @Override
    public void deleteAllGoods() {
        goodsRepository.deleteAll();
    }

    // 插入三条测试商品数据
    @Override
    public void insertTestData() {
        // 清空现有数据
        goodsRepository.deleteAll();

        // 插入三条测试商品数据
        List<Goods> testGoods = Arrays.asList(
            new Goods("Test Product 1", "This is a test product for testing purposes.", 29.99, 100),
            new Goods("Test Product 2", "Another test product for testing purposes.", 49.99, 50),
            new Goods("Test Product 3", "Yet another test product to test API functionality.", 19.99, 150)
        );
        goodsRepository.saveAll(testGoods);  // 插入测试商品数据
    }
}