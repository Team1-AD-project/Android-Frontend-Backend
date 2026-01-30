package com.example.EcoGo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.EcoGo.model.BusRoute;

public interface BusRouteRepository extends MongoRepository<BusRoute, String> {
}
