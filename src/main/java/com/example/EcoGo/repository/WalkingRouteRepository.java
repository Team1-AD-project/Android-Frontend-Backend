package com.example.EcoGo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.EcoGo.model.WalkingRoute;

public interface WalkingRouteRepository extends MongoRepository<WalkingRoute, String> {
}
