package com.example.EcoGo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.EcoGo.model.HistoryItem;

public interface HistoryRepository extends MongoRepository<HistoryItem, String> {
}
