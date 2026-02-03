package com.example.EcoGo.service;

import com.example.EcoGo.interfacemethods.LeaderboardInterface;
import com.example.EcoGo.model.Ranking;
import com.example.EcoGo.repository.RankingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LeaderboardImplementation implements LeaderboardInterface {

    private static final List<String> MOCK_PERIODS = List.of("Week 4, 2026", "Week 3, 2026", "Week 2, 2026");

    private static List<Ranking> buildMockRankings(String period) {
        List<Ranking> list = new ArrayList<>();
        String[][] rows = {
            {"user_001", "小明", "125000", "true"},
            {"user_002", "EcoRunner", "98200", "true"},
            {"user_003", "绿行侠", "87600", "false"},
            {"user_004", "步数达人", "75400", "true"},
            {"user_005", "晨跑王", "68100", "false"},
            {"user_006", "林小绿", "59200", "true"},
            {"user_007", "低碳生活", "51800", "false"},
            {"user_008", "天天走路", "44500", "false"},
            {"user_009", "校园行者", "38100", "true"},
            {"user_010", "环保先锋", "32600", "false"},
            {"user_011", "小步快跑", "27800", "false"},
            {"user_012", "绿色出行", "22100", "false"},
        };
        for (int i = 0; i < rows.length; i++) {
            Ranking r = new Ranking();
            r.setPeriod(period);
            r.setRank(i + 1);
            r.setUserId(rows[i][0]);
            r.setNickname(rows[i][1]);
            r.setSteps(Integer.parseInt(rows[i][2]));
            r.setVip(Boolean.parseBoolean(rows[i][3]));
            list.add(r);
        }
        return list;
    }

    @Autowired
    private RankingRepository rankingRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Ranking> getRankingsByPeriod(String period) {
        List<Ranking> fromDb = rankingRepository.findByPeriodOrderByRankAsc(period);
        if (fromDb != null && !fromDb.isEmpty()) {
            return fromDb;
        }
        return buildMockRankings(period);
    }

    @Override
    public List<String> getAvailablePeriods() {
        List<String> periods = mongoTemplate.query(Ranking.class)
                .distinct("period")
                .as(String.class)
                .all();
        if (periods != null && !periods.isEmpty()) {
            return periods;
        }
        return new ArrayList<>(MOCK_PERIODS);
    }
}
