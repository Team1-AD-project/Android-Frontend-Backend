package com.example.EcoGo.repository;

import com.example.EcoGo.model.Advertisement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AdvertisementRepository extends MongoRepository<Advertisement, String> {
    List<Advertisement> findByStatus(String status);
    List<Advertisement> findByName(String name);
}
