package com.example.EcoGo.repository;

import com.example.EcoGo.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {

    List<Trip> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Trip> findByUserIdAndCarbonStatus(String userId, String carbonStatus);

    List<Trip> findByUserId(String userId);

    List<Trip> findByUserIdAndIsGreenTrip(String userId, boolean isGreenTrip);
}
