package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.dto.PointsDto;
import com.example.EcoGo.model.UserPointsLog;

import java.util.List;

public interface PointsService {

    /**
     * Adjust user points (Gain or Deduct)
     * 
     * @param userId      The User UUID (or business ID? Need to be careful.
     *                    Typically Service uses UUID internally, but DTO might pass
     *                    Business ID. I'll assume UUID for internal service
     *                    consistency, but will handle resolution if needed)
     *                    Let's stick to UUID for internal service calls to be safe.
     * @param points      Amount (+ or -)
     * @param source      Source of change
     * @param description Brief description
     * @param adminAction Optional admin details
     */
    UserPointsLog adjustPoints(String userId, long points, String source, String description,
            UserPointsLog.AdminAction adminAction);

    /**
     * Get current points balance
     */
    PointsDto.CurrentPointsResponse getCurrentPoints(String userId);

    /**
     * Get points history
     */
    List<PointsDto.PointsLogResponse> getPointsHistory(String userId);
}
