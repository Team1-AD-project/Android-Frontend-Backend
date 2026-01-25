package com.example.EcoGo.repository;

import com.example.EcoGo.model.Ranking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RankingRepository extends MongoRepository<Ranking, String> {
    List<Ranking> findByPeriodOrderByRankAsc(String period);
}
