package com.example.EcoGo.service;

import com.example.EcoGo.interfacemethods.CarbonFootprintInterface;
import com.example.EcoGo.model.CarbonFootprint;
import com.example.EcoGo.repository.CarbonFootprintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CarbonFootprintImplementation implements CarbonFootprintInterface {

    @Autowired
    private CarbonFootprintRepository carbonFootprintRepository;

    @Override
    public CarbonFootprint getCarbonFootprint(String userId, String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate, endDate;

        switch (period.toLowerCase()) {
            case "daily":
                startDate = today;
                endDate = today;
                break;
            case "weekly":
                startDate = today.minusDays(7);
                endDate = today;
                break;
            case "monthly":
            default:
                startDate = today.withDayOfMonth(1);
                endDate = today;
                break;
        }

        Optional<CarbonFootprint> footprint = carbonFootprintRepository
                .findByUserIdAndPeriodAndStartDateAndEndDate(userId, period, startDate, endDate);

        return footprint.orElseGet(() -> {
            CarbonFootprint newFootprint = new CarbonFootprint(userId, period, startDate, endDate);
            return carbonFootprintRepository.save(newFootprint);
        });
    }

    @Override
    public CarbonFootprint recordTrip(String userId, String tripType, Float distance) {
        CarbonFootprint footprint = getCarbonFootprint(userId, "monthly");

        // 计算CO2节省量（基于出行方式和距离）
        Float co2Amount = calculateCO2Saved(tripType, distance);
        footprint.addTrip(tripType, co2Amount);

        return carbonFootprintRepository.save(footprint);
    }

    @Override
    public CarbonFootprint calculateMonthlyFootprint(String userId, LocalDate startDate, LocalDate endDate) {
        Optional<CarbonFootprint> footprint = carbonFootprintRepository
                .findByUserIdAndPeriodAndStartDateAndEndDate(userId, "monthly", startDate, endDate);

        if (footprint.isPresent()) {
            return footprint.get();
        }

        CarbonFootprint newFootprint = new CarbonFootprint(userId, "monthly", startDate, endDate);
        return carbonFootprintRepository.save(newFootprint);
    }

    /**
     * 计算CO2节省量
     * 参考：私家车平均每公里排放约0.2kg CO2
     */
    private Float calculateCO2Saved(String tripType, Float distance) {
        float carEmission = 0.2f; // kg CO2 per km
        switch (tripType.toLowerCase()) {
            case "bus":
                return distance * carEmission * 0.5f; // 巴士节省50%
            case "walking":
            case "bicycle":
                return distance * carEmission; // 步行/骑行节省100%
            default:
                return 0f;
        }
    }
}
