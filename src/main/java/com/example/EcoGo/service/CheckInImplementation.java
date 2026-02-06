package com.example.EcoGo.service;

import com.example.EcoGo.interfacemethods.CheckInInterface;
import com.example.EcoGo.model.CheckIn;
import com.example.EcoGo.repository.CheckInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CheckInImplementation implements CheckInInterface {

    @Autowired
    private CheckInRepository checkInRepository;

    @Override
    public CheckIn performCheckIn(String userId) {
        LocalDate today = LocalDate.now();
        
        // 检查今天是否已签到
        Optional<CheckIn> existingCheckIn = checkInRepository.findByUserIdAndCheckInDate(userId, today);
        if (existingCheckIn.isPresent()) {
            return existingCheckIn.get();
        }

        // 计算连续签到天数
        Integer consecutiveDays = getConsecutiveDays(userId) + 1;

        // 创建新的签到记录
        CheckIn checkIn = new CheckIn(userId, today, consecutiveDays);
        return checkInRepository.save(checkIn);
    }

    @Override
    public CheckIn getCheckInStatus(String userId, LocalDate date) {
        return checkInRepository.findByUserIdAndCheckInDate(userId, date).orElse(null);
    }

    @Override
    public List<CheckIn> getUserCheckInHistory(String userId) {
        return checkInRepository.findByUserIdOrderByCheckInDateDesc(userId);
    }

    @Override
    public Integer getConsecutiveDays(String userId) {
        List<CheckIn> history = checkInRepository.findByUserIdOrderByCheckInDateDesc(userId);
        if (history.isEmpty()) {
            return 0;
        }

        int consecutiveDays = 0;
        LocalDate expectedDate = LocalDate.now().minusDays(1);

        for (CheckIn checkIn : history) {
            if (checkIn.getCheckInDate().equals(expectedDate)) {
                consecutiveDays++;
                expectedDate = expectedDate.minusDays(1);
            } else {
                break;
            }
        }

        return consecutiveDays;
    }
}
