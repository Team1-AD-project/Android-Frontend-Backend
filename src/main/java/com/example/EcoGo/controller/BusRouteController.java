package com.example.EcoGo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.model.BusRoute;
import com.example.EcoGo.repository.BusRouteRepository;

@RestController
@RequestMapping("/api/v1/routes")
public class BusRouteController {
    private final BusRouteRepository busRouteRepository;

    public BusRouteController(BusRouteRepository busRouteRepository) {
        this.busRouteRepository = busRouteRepository;
    }

    @GetMapping
    public ResponseMessage<List<BusRoute>> getRoutes() {
        return ResponseMessage.success(busRouteRepository.findAll());
    }
}
