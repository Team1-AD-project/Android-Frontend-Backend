package com.example.EcoGo.service;

import com.example.EcoGo.interfacemethods.DailyGoalInterface;
import com.example.EcoGo.model.DailyGoal;
import com.example.EcoGo.repository.DailyGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DailyGoalImplementation implements DailyGoalInterface {

    @Autowired
    private DailyGoalRepository dailyGoalRepository;

    @Override
    public DailyGoal getDailyGoal(String userId, LocalDate date) {
        Optional<DailyGoal> goal = dailyGoalRepository.findByUserIdAndDate(userId, date);
        return goal.orElseGet(() -> createOrUpdateGoal(userId, date));
    }

    @Override
    public DailyGoal updateDailyGoal(String userId, Integer steps, Integer trips, Float co2Saved) {
        LocalDate today = LocalDate.now();
        DailyGoal goal = getDailyGoal(userId, today);
        
        if (steps != null) {
            goal.setCurrentSteps(steps);
        }
        if (trips != null) {
            goal.setCurrentTrips(trips);
        }
        if (co2Saved != null) {
            goal.setCurrentCo2Saved(co2Saved);
        }
        
        goal.setUpdatedAt(LocalDateTime.now());
        return dailyGoalRepository.save(goal);
    }

    @Override
    public DailyGoal createOrUpdateGoal(String userId, LocalDate date) {
        Optional<DailyGoal> existing = dailyGoalRepository.findByUserIdAndDate(userId, date);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        DailyGoal newGoal = new DailyGoal(userId, date);
        return dailyGoalRepository.save(newGoal);
    }
}
