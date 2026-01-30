package com.example.EcoGo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.model.WalkingRoute;
import com.example.EcoGo.repository.WalkingRouteRepository;

@RestController
@RequestMapping("/api/v1/walking-routes")
public class WalkingRouteController {
    private final WalkingRouteRepository walkingRouteRepository;

    public WalkingRouteController(WalkingRouteRepository walkingRouteRepository) {
        this.walkingRouteRepository = walkingRouteRepository;
    }

    @GetMapping
    public ResponseMessage<List<WalkingRoute>> getWalkingRoutes() {
        return ResponseMessage.success(walkingRouteRepository.findAll());
    }
}
