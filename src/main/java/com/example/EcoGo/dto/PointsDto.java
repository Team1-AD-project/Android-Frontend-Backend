package com.example.EcoGo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PointsDto {

    public static class AdjustPointsRequest {
        public long points; // Amount (+ or -)
        public String source; // admin, trip, etc
        public String description;
        // For admin action details
        public String operator_id;
        public String reason;
    }

    public static class CurrentPointsResponse {
        public String userId;
        public long currentPoints;
        public long totalPoints; // Historical total (only adds, never subtracts)

        public CurrentPointsResponse(String userId, long current, long total) {
            this.userId = userId;
            this.currentPoints = current;
            this.totalPoints = total;
        }
    }

    public static class PointsLogResponse {
        public String id;
        public String change_type;
        public long points;
        public String source;
        public String description; // Actually not in Model but useful? Or maybe mapped from source/reason
        public long balance_after;
        public String created_at;

        // Admin details if any
        public AdminActionDto admin_action;
    }

    public static class AdminActionDto {
        public String operator_id;
        public String reason;
        public String approval_status;
    }

    public static class TripStatsResponse {
        public long totalTrips;
        public long totalPointsEarned;

        public TripStatsResponse(long trips, long points) {
            this.totalTrips = trips;
            this.totalPointsEarned = points;
        }
    }
}
