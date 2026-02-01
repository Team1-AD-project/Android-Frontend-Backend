package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.DailyGoal;
import java.time.LocalDate;

public interface DailyGoalInterface {
    DailyGoal getDailyGoal(String userId, LocalDate date);
    DailyGoal updateDailyGoal(String userId, Integer steps, Integer trips, Float co2Saved);
    DailyGoal createOrUpdateGoal(String userId, LocalDate date);
}
