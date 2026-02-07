package com.example.EcoGo.controller;


import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;
    private static final double SG_LAT = 1.3521;
    private static final double SG_LON = 103.8198;
  
    @GetMapping
    public ResponseMessage<Object> getCurrentWeather() {
        try {
            return ResponseMessage.success(weatherService.getWeather(SG_LAT, SG_LON));
        } catch (Exception e) {
            return new ResponseMessage<>(500, "获取天气信息失败: " + e.getMessage(), null);
        }
    }
}