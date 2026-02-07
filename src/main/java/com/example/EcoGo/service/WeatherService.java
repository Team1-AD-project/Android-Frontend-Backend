package com.example.EcoGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    @Autowired
    private RestTemplate restTemplate;

@Value("${weather.api.key}")
    private String apiKey = "";

    @Value("${weather.api.url}")
    private String weatherUrl = "";

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWeather(double lat, double lon) {
        Map<String, Object> finalResult = new HashMap<>();

        try {
            // 1. 获取基础天气
            Map<String, Object> weatherData = callWeatherApi(lat, lon);
            
            // 2. 获取空气质量 (AQI)
            Map<String, Object> pollutionData = callPollutionApi(lat, lon);

            // 3. 组装数据
            if (weatherData != null) {
                // 温度 (四舍五入取整)
                Map<String, Object> main = (Map<String, Object>) weatherData.get("main");
                if (main != null) {
                    double temp = Double.parseDouble(main.get("temp").toString());
                    finalResult.put("temperature", Math.round(temp));
                    finalResult.put("humidity", main.get("humidity") + "%");
                }

                // 天气状况 & 图标
                List<Map<String, Object>> wList = (List<Map<String, Object>>) weatherData.get("weather");
                if (wList != null && !wList.isEmpty()) {
                    finalResult.put("condition", wList.get(0).get("main")); // e.g., Clear, Rain
                    finalResult.put("description", wList.get(0).get("description")); // e.g., overcast clouds
                    finalResult.put("icon", wList.get(0).get("icon"));
                }
            }

            // 4. 处理 AQI
            if (pollutionData != null) {
                List<Map<String, Object>> pList = (List<Map<String, Object>>) pollutionData.get("list");
                if (pList != null && !pList.isEmpty()) {
                    Map<String, Object> pMain = (Map<String, Object>) pList.get(0).get("main");
                    // OpenWeatherMap 返回 1(好) -> 5(差)
                    finalResult.put("aqiLevel", pMain.get("aqi")); 
                    finalResult.put("aqiText", "AQI Level " + pMain.get("aqi")); 
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取天气服务异常");
        }

        return finalResult;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callWeatherApi(double lat, double lon) {
        String url = UriComponentsBuilder.fromUriString(weatherUrl+"")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();
        return restTemplate.getForObject(url, Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callPollutionApi(double lat, double lon) {
        String pollutionUrl = weatherUrl.replace("/weather", "/air_pollution");
        String url = UriComponentsBuilder.fromUriString(pollutionUrl+"")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .toUriString();
        return restTemplate.getForObject(url, Map.class);
    }
}