package com.example.EcoGo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.EcoGo.dto.RecommendationRequestDto;
import com.example.EcoGo.dto.RecommendationResponseDto;
import com.example.EcoGo.dto.ResponseMessage;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    @PostMapping
    public ResponseMessage<RecommendationResponseDto> recommend(@RequestBody RecommendationRequestDto request) {
        String dest = request.getDestination() == null ? "" : request.getDestination().toLowerCase();
        RecommendationResponseDto rec;
        if (dest.contains("library") || dest.contains("study")) {
            rec = new RecommendationResponseDto(
                "It's a nice day! Walking to " + request.getDestination() + " takes 15 mins and earns 50 Green Pts.",
                "Eco-Choice"
            );
        } else if (dest.contains("gym") || dest.contains("sport")) {
            rec = new RecommendationResponseDto(
                "Warm up with a light jog to " + request.getDestination() + ". It's only 1.2km away!",
                "Healthy"
            );
        } else {
            rec = new RecommendationResponseDto(
                "Take Bus D1 (3 min wait). It's the fastest way to " + request.getDestination() + "!",
                "Fastest"
            );
        }
        return ResponseMessage.success(rec);
    }
}
