package com.example.EcoGo.repository;

import com.example.EcoGo.model.UserPointsLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPointsLogRepository extends MongoRepository<UserPointsLog, String> {

    // Find logs by userId, sorted by time desc
    List<UserPointsLog> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find logs within date range if needed (future)
    // List<UserPointsLog> findByUserIdAndCreatedAtBetween(String userId,
    // LocalDateTime start, LocalDateTime end);
}
