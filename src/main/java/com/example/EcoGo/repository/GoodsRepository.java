package com.example.EcoGo.repository;

import com.example.EcoGo.model.Goods;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoodsRepository extends MongoRepository<Goods, String> {
    // You can add custom query methods here if needed
}