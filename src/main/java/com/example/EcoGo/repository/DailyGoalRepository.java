package com.example.EcoGo.repository;

import com.example.EcoGo.model.DailyGoal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyGoalRepository extends MongoRepository<DailyGoal, String> {
    Optional<DailyGoal> findByUserIdAndDate(String userId, LocalDate date);
    List<DailyGoal> findByUserIdOrderByDateDesc(String userId);
    List<DailyGoal> findByUserId(String userId);
}
