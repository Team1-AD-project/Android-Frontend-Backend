package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.LeaderboardInterface;
import com.example.EcoGo.model.Ranking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboards")
public class LeaderboardController {
    private static final Logger logger = LoggerFactory.getLogger(LeaderboardController.class);

    @Autowired
    private LeaderboardInterface leaderboardService;

    /**
     * Get a list of all available, unique periods.
     * GET /api/v1/leaderboards/periods
     */
    @GetMapping("/periods")
    public ResponseMessage<List<String>> getAvailablePeriods() {
        logger.info("Fetching all available leaderboard periods");
        List<String> periods = leaderboardService.getAvailablePeriods();
        return ResponseMessage.success(periods);
    }

    /**
     * Get all rankings for a specific period.
     * GET /api/v1/leaderboards/rankings?period=Week 4, 2026
     */
    @GetMapping("/rankings")
    public ResponseMessage<List<Ranking>> getRankingsByPeriod(@RequestParam String period) {
        logger.info("Fetching rankings for period: {}", period);
        List<Ranking> rankings = leaderboardService.getRankingsByPeriod(period);
        return ResponseMessage.success(rankings);
    }
}
