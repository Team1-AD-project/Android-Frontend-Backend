package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.Ranking;
import java.util.List;

public interface LeaderboardInterface {

    /**
     * Gets all ranking entries for a specific period (e.g., a specific week).
     * @param period The period identifier, e.g., "Week 4, 2026".
     * @return A list of rankings for that period, ordered by rank.
     */
    List<Ranking> getRankingsByPeriod(String period);

    /**
     * Gets a list of all available, distinct periods.
     * @return A list of unique period strings, e.g., ["Week 4, 2026", "Week 3, 2026"].
     */
    List<String> getAvailablePeriods();

    // Note: Creating, updating, or deleting single rank entries might not be a direct user action.
    // These operations are usually the result of a backend calculation process.
    // We will keep the interface simple for now, focusing on retrieving data for the UI.
}
