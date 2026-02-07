package com.example.EcoGo.repository;

import com.example.EcoGo.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    Inventory findByGoodsId(String goodsId);
}
