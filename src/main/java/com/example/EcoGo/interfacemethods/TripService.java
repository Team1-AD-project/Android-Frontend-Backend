package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.dto.TripDto;
import com.example.EcoGo.model.Trip;

import java.util.List;

public interface TripService {

    /**
     * Start a new trip (status = tracking)
     */
    Trip startTrip(String userId, TripDto.StartTripRequest request);

    /**
     * Complete a trip: fill in end data, settle points via PointsService.settleTrip
     */
    Trip completeTrip(String userId, String tripId, TripDto.CompleteTripRequest request);

    /**
     * Cancel a trip
     */
    void cancelTrip(String userId, String tripId);

    /**
     * Get trip detail by ID
     */
    TripDto.TripResponse getTripById(String userId, String tripId);

    /**
     * Get user's trip list (summaries)
     */
    List<TripDto.TripSummaryResponse> getUserTrips(String userId);

    /**
     * Get user's currently tracking trip (if any)
     */
    TripDto.TripResponse getCurrentTrip(String userId);

    // --- Admin ---

    /**
     * Admin: get all trips
     */
    List<TripDto.TripSummaryResponse> getAllTrips();

    /**
     * Admin: get trips by user
     */
    List<TripDto.TripSummaryResponse> getTripsByUser(String userId);
}
