package com.example.EcoGo.repository;

import com.example.EcoGo.model.CarbonFootprint;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CarbonFootprintRepository extends MongoRepository<CarbonFootprint, String> {
    Optional<CarbonFootprint> findByUserIdAndPeriodAndStartDateAndEndDate(
            String userId, String period, LocalDate startDate, LocalDate endDate);
    List<CarbonFootprint> findByUserIdOrderByStartDateDesc(String userId);
    List<CarbonFootprint> findByUserIdAndPeriod(String userId, String period);
}
