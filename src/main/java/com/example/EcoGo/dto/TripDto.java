package com.example.EcoGo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TripDto {

    // === Request: 开始行程 ===
    public static class StartTripRequest {
        public double startLng;
        public double startLat;
        public String startAddress;
        public String startPlaceName;
        public String startCampusZone;
    }

    // === Request: 完成行程 ===
    public static class CompleteTripRequest {
        public double endLng;
        public double endLat;
        public String endAddress;
        public String endPlaceName;
        public String endCampusZone;
        public double distance;                     // 行程总距离(km)
        public List<TransportSegmentDto> transportModes;  // 各交通段
        public List<GeoPointDto> polylinePoints;    // 轨迹点数组
        public String detectedMode;                 // ML识别的主要交通方式
        public double mlConfidence;                 // ML置信度
        public boolean isGreenTrip;                 // 是否绿色出行
        public long carbonSaved;                    // 碳减排量(g)
    }

    // === Sub DTOs ===
    public static class TransportSegmentDto {
        public String mode;
        public double subDistance;
        public int subDuration;
    }

    public static class GeoPointDto {
        public double lng;
        public double lat;
    }

    public static class LocationDetailDto {
        public String address;
        public String placeName;
        public String campusZone;
    }

    // === Response: 行程详情 ===
    public static class TripResponse {
        public String id;
        public String userId;
        public GeoPointDto startPoint;
        public GeoPointDto endPoint;
        public LocationDetailDto startLocation;
        public LocationDetailDto endLocation;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public List<TransportSegmentDto> transportModes;
        public String detectedMode;
        public double mlConfidence;
        public boolean isGreenTrip;
        public double distance;
        public List<GeoPointDto> polylinePoints;
        public long carbonSaved;
        public long pointsGained;
        public String carbonStatus;
        public LocalDateTime createdAt;
    }

    // === Response: 行程摘要（列表用） ===
    public static class TripSummaryResponse {
        public String id;
        public String userId;
        public String startPlaceName;
        public String endPlaceName;
        public String detectedMode;
        public double distance;
        public long carbonSaved;
        public long pointsGained;
        public boolean isGreenTrip;
        public String carbonStatus;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
    }
}
