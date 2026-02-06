package com.example.EcoGo.repository;

import com.example.EcoGo.model.CheckIn;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends MongoRepository<CheckIn, String> {
    Optional<CheckIn> findByUserIdAndCheckInDate(String userId, LocalDate checkInDate);
    List<CheckIn> findByUserIdOrderByCheckInDateDesc(String userId);
    List<CheckIn> findByUserId(String userId);
    long countByUserId(String userId);
}
