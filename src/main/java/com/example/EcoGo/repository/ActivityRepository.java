package com.example.EcoGo.repository;

import com.example.EcoGo.model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, String> {
    List<Activity> findByStatus(String status);
    List<Activity> findByType(String type);
    List<Activity> findByStatusIn(List<String> statuses);
}
