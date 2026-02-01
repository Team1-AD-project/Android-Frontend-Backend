package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.CheckIn;
import java.time.LocalDate;
import java.util.List;

public interface CheckInInterface {
    CheckIn performCheckIn(String userId);
    CheckIn getCheckInStatus(String userId, LocalDate date);
    List<CheckIn> getUserCheckInHistory(String userId);
    Integer getConsecutiveDays(String userId);
}
