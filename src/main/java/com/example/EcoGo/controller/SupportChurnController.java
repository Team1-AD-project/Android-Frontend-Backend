package com.example.EcoGo.controller;

import com.example.EcoGo.service.churn.ChurnRiskLevel;
import com.example.EcoGo.service.churn.SupportService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/support")
public class SupportChurnController {

    private final SupportService supportService;

    public SupportChurnController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/churn")
    public Map<String, String> churn(@RequestParam String userId) {
        ChurnRiskLevel level = supportService.getChurnRiskLevel(userId);
        return Map.of("userId", userId, "riskLevel", level.name());
    }
}
