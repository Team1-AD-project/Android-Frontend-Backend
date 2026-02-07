package com.example.EcoGo.service;

import com.example.EcoGo.dto.PointsDto;
import com.example.EcoGo.dto.TripDto;
import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.PointsService;
import com.example.EcoGo.interfacemethods.TripService;
import com.example.EcoGo.model.Trip;
import com.example.EcoGo.repository.TripRepository;
import com.example.EcoGo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TripServiceImpl implements TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointsService pointsService;

    @Override
    public Trip startTrip(String userId, TripDto.StartTripRequest request) {
        // Verify user exists
        userRepository.findByUserid(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Trip trip = new Trip();
        trip.setId(UUID.randomUUID().toString());
        trip.setUserId(userId);
        trip.setStartPoint(new Trip.GeoPoint(request.startLng, request.startLat));
        trip.setStartLocation(new Trip.LocationDetail(
                request.startAddress, request.startPlaceName, request.startCampusZone));
        trip.setStartTime(LocalDateTime.now());
        trip.setCarbonStatus("tracking");
        trip.setCreatedAt(LocalDateTime.now());

        return tripRepository.save(trip);
    }

    @Override
    public Trip completeTrip(String userId, String tripId, TripDto.CompleteTripRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
        if (!"tracking".equals(trip.getCarbonStatus())) {
            throw new BusinessException(ErrorCode.TRIP_STATUS_ERROR, trip.getCarbonStatus());
        }

        // Fill end data
        trip.setEndPoint(new Trip.GeoPoint(request.endLng, request.endLat));
        trip.setEndLocation(new Trip.LocationDetail(
                request.endAddress, request.endPlaceName, request.endCampusZone));
        trip.setEndTime(LocalDateTime.now());
        trip.setDistance(request.distance);
        trip.setDetectedMode(request.detectedMode);
        trip.setMlConfidence(request.mlConfidence);
        trip.setGreenTrip(request.isGreenTrip);
        trip.setCarbonSaved(request.carbonSaved);

        // Convert transport segments
        if (request.transportModes != null) {
            List<Trip.TransportSegment> segments = request.transportModes.stream()
                    .map(s -> new Trip.TransportSegment(s.mode, s.subDistance, s.subDuration))
                    .collect(Collectors.toList());
            trip.setTransportModes(segments);
        }

        // Convert polyline points
        if (request.polylinePoints != null) {
            List<Trip.GeoPoint> points = request.polylinePoints.stream()
                    .map(p -> new Trip.GeoPoint(p.lng, p.lat))
                    .collect(Collectors.toList());
            trip.setPolylinePoints(points);
        }

        // Calculate points
        long pointsGained = pointsService.calculatePoints(request.detectedMode, request.distance);

        // Format trip description
        String startPlace = trip.getStartLocation() != null ? trip.getStartLocation().getPlaceName() : "Unknown";
        String endPlace = request.endPlaceName != null ? request.endPlaceName : "Unknown";
        String description = String.format("Trip: %s → %s (%.2f km)", startPlace, endPlace, request.distance);

        // Use adjustPoints to handle points settlement
        pointsService.adjustPoints(userId, pointsGained, "trip", description, null);
        trip.setPointsGained(pointsGained);
        trip.setCarbonStatus("completed");

        return tripRepository.save(trip);
    }

    @Override
    public void cancelTrip(String userId, String tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }
        if (!"tracking".equals(trip.getCarbonStatus())) {
            throw new BusinessException(ErrorCode.TRIP_STATUS_ERROR, trip.getCarbonStatus());
        }

        trip.setCarbonStatus("canceled");
        trip.setEndTime(LocalDateTime.now());
        tripRepository.save(trip);
    }

    @Override
    public TripDto.TripResponse getTripById(String userId, String tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TRIP_NOT_FOUND));

        if (!trip.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        return convertToResponse(trip);
    }

    @Override
    public List<TripDto.TripSummaryResponse> getUserTrips(String userId) {
        List<Trip> trips = tripRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return trips.stream().map(this::convertToSummary).collect(Collectors.toList());
    }

    @Override
    public TripDto.TripResponse getCurrentTrip(String userId) {
        List<Trip> trackingTrips = tripRepository.findByUserIdAndCarbonStatus(userId, "tracking");
        if (trackingTrips.isEmpty()) {
            return null;
        }
        return convertToResponse(trackingTrips.get(0));
    }

    @Override
    public List<TripDto.TripSummaryResponse> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(this::convertToSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDto.TripSummaryResponse> getTripsByUser(String userId) {
        return tripRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToSummary)
                .collect(Collectors.toList());
    }

    // --- Converters ---

    private TripDto.TripResponse convertToResponse(Trip trip) {
        TripDto.TripResponse resp = new TripDto.TripResponse();
        resp.id = trip.getId();
        resp.userId = trip.getUserId();
        resp.startTime = trip.getStartTime();
        resp.endTime = trip.getEndTime();
        resp.detectedMode = trip.getDetectedMode();
        resp.mlConfidence = trip.getMlConfidence();
        resp.isGreenTrip = trip.isGreenTrip();
        resp.distance = trip.getDistance();
        resp.carbonSaved = trip.getCarbonSaved();
        resp.pointsGained = trip.getPointsGained();
        resp.carbonStatus = trip.getCarbonStatus();
        resp.createdAt = trip.getCreatedAt();

        if (trip.getStartPoint() != null) {
            TripDto.GeoPointDto sp = new TripDto.GeoPointDto();
            sp.lng = trip.getStartPoint().getLng();
            sp.lat = trip.getStartPoint().getLat();
            resp.startPoint = sp;
        }
        if (trip.getEndPoint() != null) {
            TripDto.GeoPointDto ep = new TripDto.GeoPointDto();
            ep.lng = trip.getEndPoint().getLng();
            ep.lat = trip.getEndPoint().getLat();
            resp.endPoint = ep;
        }
        if (trip.getStartLocation() != null) {
            TripDto.LocationDetailDto sl = new TripDto.LocationDetailDto();
            sl.address = trip.getStartLocation().getAddress();
            sl.placeName = trip.getStartLocation().getPlaceName();
            sl.campusZone = trip.getStartLocation().getCampusZone();
            resp.startLocation = sl;
        }
        if (trip.getEndLocation() != null) {
            TripDto.LocationDetailDto el = new TripDto.LocationDetailDto();
            el.address = trip.getEndLocation().getAddress();
            el.placeName = trip.getEndLocation().getPlaceName();
            el.campusZone = trip.getEndLocation().getCampusZone();
            resp.endLocation = el;
        }
        if (trip.getTransportModes() != null) {
            resp.transportModes = trip.getTransportModes().stream().map(s -> {
                TripDto.TransportSegmentDto dto = new TripDto.TransportSegmentDto();
                dto.mode = s.getMode();
                dto.subDistance = s.getSubDistance();
                dto.subDuration = s.getSubDuration();
                return dto;
            }).collect(Collectors.toList());
        }
        if (trip.getPolylinePoints() != null) {
            resp.polylinePoints = trip.getPolylinePoints().stream().map(p -> {
                TripDto.GeoPointDto dto = new TripDto.GeoPointDto();
                dto.lng = p.getLng();
                dto.lat = p.getLat();
                return dto;
            }).collect(Collectors.toList());
        }

        return resp;
    }

    private TripDto.TripSummaryResponse convertToSummary(Trip trip) {
        TripDto.TripSummaryResponse resp = new TripDto.TripSummaryResponse();
        resp.id = trip.getId();
        resp.userId = trip.getUserId();
        resp.detectedMode = trip.getDetectedMode();
        resp.distance = trip.getDistance();
        resp.carbonSaved = trip.getCarbonSaved();
        resp.pointsGained = trip.getPointsGained();
        resp.isGreenTrip = trip.isGreenTrip();
        resp.carbonStatus = trip.getCarbonStatus();
        resp.startTime = trip.getStartTime();
        resp.endTime = trip.getEndTime();

        if (trip.getStartLocation() != null) {
            resp.startPlaceName = trip.getStartLocation().getPlaceName();
        }
        if (trip.getEndLocation() != null) {
            resp.endPlaceName = trip.getEndLocation().getPlaceName();
        }

        return resp;
    }
}
