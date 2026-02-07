package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "trips")
public class Trip {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    // 起点/终点经纬度
    @Field("start_point")
    private GeoPoint startPoint;

    @Field("end_point")
    private GeoPoint endPoint;

    // 起点/终点完整地址信息
    @Field("start_location")
    private LocationDetail startLocation;

    @Field("end_location")
    private LocationDetail endLocation;

    // 时间
    @Field("start_time")
    private LocalDateTime startTime;

    @Field("end_time")
    private LocalDateTime endTime;

    // 交通方式段落
    @Field("transport_modes")
    private List<TransportSegment> transportModes;

    // ML识别结果
    @Field("detected_mode")
    private String detectedMode;

    @Field("ml_confidence")
    private double mlConfidence;

    @Field("is_green_trip")
    private boolean isGreenTrip;

    // 行程核心数据
    private double distance;

    @Field("polyline_points")
    private List<GeoPoint> polylinePoints;

    @Field("carbon_saved")
    private long carbonSaved;

    @Field("points_gained")
    private long pointsGained;

    @Field("carbon_status")
    private String carbonStatus; // tracking, completed, canceled

    @Field("created_at")
    private LocalDateTime createdAt;

    // --- Nested Classes ---

    public static class GeoPoint {
        private double lng;
        private double lat;

        public GeoPoint() {}

        public GeoPoint(double lng, double lat) {
            this.lng = lng;
            this.lat = lat;
        }

        public double getLng() { return lng; }
        public void setLng(double lng) { this.lng = lng; }
        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }
    }

    public static class LocationDetail {
        private String address;
        @Field("place_name")
        private String placeName;
        @Field("campus_zone")
        private String campusZone;

        public LocationDetail() {}

        public LocationDetail(String address, String placeName, String campusZone) {
            this.address = address;
            this.placeName = placeName;
            this.campusZone = campusZone;
        }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPlaceName() { return placeName; }
        public void setPlaceName(String placeName) { this.placeName = placeName; }
        public String getCampusZone() { return campusZone; }
        public void setCampusZone(String campusZone) { this.campusZone = campusZone; }
    }

    public static class TransportSegment {
        private String mode;
        @Field("sub_distance")
        private double subDistance;
        @Field("sub_duration")
        private int subDuration;

        public TransportSegment() {}

        public TransportSegment(String mode, double subDistance, int subDuration) {
            this.mode = mode;
            this.subDistance = subDistance;
            this.subDuration = subDuration;
        }

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public double getSubDistance() { return subDistance; }
        public void setSubDistance(double subDistance) { this.subDistance = subDistance; }
        public int getSubDuration() { return subDuration; }
        public void setSubDuration(int subDuration) { this.subDuration = subDuration; }
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public GeoPoint getStartPoint() { return startPoint; }
    public void setStartPoint(GeoPoint startPoint) { this.startPoint = startPoint; }

    public GeoPoint getEndPoint() { return endPoint; }
    public void setEndPoint(GeoPoint endPoint) { this.endPoint = endPoint; }

    public LocationDetail getStartLocation() { return startLocation; }
    public void setStartLocation(LocationDetail startLocation) { this.startLocation = startLocation; }

    public LocationDetail getEndLocation() { return endLocation; }
    public void setEndLocation(LocationDetail endLocation) { this.endLocation = endLocation; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public List<TransportSegment> getTransportModes() { return transportModes; }
    public void setTransportModes(List<TransportSegment> transportModes) { this.transportModes = transportModes; }

    public String getDetectedMode() { return detectedMode; }
    public void setDetectedMode(String detectedMode) { this.detectedMode = detectedMode; }

    public double getMlConfidence() { return mlConfidence; }
    public void setMlConfidence(double mlConfidence) { this.mlConfidence = mlConfidence; }

    public boolean isGreenTrip() { return isGreenTrip; }
    public void setGreenTrip(boolean greenTrip) { isGreenTrip = greenTrip; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public List<GeoPoint> getPolylinePoints() { return polylinePoints; }
    public void setPolylinePoints(List<GeoPoint> polylinePoints) { this.polylinePoints = polylinePoints; }

    public long getCarbonSaved() { return carbonSaved; }
    public void setCarbonSaved(long carbonSaved) { this.carbonSaved = carbonSaved; }

    public long getPointsGained() { return pointsGained; }
    public void setPointsGained(long pointsGained) { this.pointsGained = pointsGained; }

    public String getCarbonStatus() { return carbonStatus; }
    public void setCarbonStatus(String carbonStatus) { this.carbonStatus = carbonStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
