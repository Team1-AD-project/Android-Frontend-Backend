package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.CarbonFootprint;
import java.time.LocalDate;

public interface CarbonFootprintInterface {
    CarbonFootprint getCarbonFootprint(String userId, String period);
    CarbonFootprint recordTrip(String userId, String tripType, Float distance);
    CarbonFootprint calculateMonthlyFootprint(String userId, LocalDate startDate, LocalDate endDate);
}
