package com.example.EcoGo.service;

import com.example.EcoGo.interfacemethods.LeaderboardInterface;
import com.example.EcoGo.model.Ranking;
import com.example.EcoGo.repository.RankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LeaderboardImplementation implements LeaderboardInterface {

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private MongoTemplate mongoTemplate; // Injected for distinct queries

    /**
     * Gets all ranking entries for a specific period, sorted by rank.
     */
    @Override
    public List<Ranking> getRankingsByPeriod(String period) {
        return rankingRepository.findByPeriodOrderByRankAsc(period);
    }

    /**
     * Gets a list of all available, unique period strings from the rankings collection.
     */
    @Override
    public List<String> getAvailablePeriods() {
        // Use MongoTemplate to perform a distinct query on the 'period' field
        List<String> periods = mongoTemplate.query(Ranking.class)
                .distinct("period")
                .as(String.class)
                .all();
        return periods;
    }
}
