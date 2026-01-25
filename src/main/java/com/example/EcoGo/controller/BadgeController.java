package com.example.EcoGo.controller;

import com.example.EcoGo.model.Badge;

import com.example.EcoGo.service.BadgeServiceImpl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class BadgeController {

    @Autowired private BadgeServiceImpl badgeService;

    // 购买
    @PostMapping("/mobile/badges/{badge_id}/purchase")
    public Map<String, Object> purchaseBadge(@PathVariable("badge_id") String badgeId, @RequestBody Map<String, String> payload) {
        try {
            return Map.of("code", 200, "data", badgeService.purchaseBadge(payload.get("user_id"), badgeId));
        } catch (Exception e) {
            return Map.of("code", 400, "msg", e.getMessage());
        }
    }

    // 佩戴/卸下 (含互斥)
    @PutMapping("/mobile/badges/{badge_id}/display")
    public Map<String, Object> toggleDisplay(@PathVariable("badge_id") String badgeId, @RequestBody Map<String, Object> payload) {
        try {
            return Map.of("code", 200, "data", badgeService.toggleBadgeDisplay((String)payload.get("user_id"), badgeId, (Boolean)payload.get("is_display")));
        } catch (Exception e) {
            return Map.of("code", 400, "msg", e.getMessage());
        }
    }

    // 商城列表
    @GetMapping("/mobile/badges/shop")
    public Map<String, Object> getShopList() {
        return Map.of("code", 200, "data", badgeService.getShopList());
    }

    // 我的背包
    @GetMapping("/mobile/badges/user/{user_id}")
    public Map<String, Object> getMyBadges(@PathVariable("user_id") String userId) {
        return Map.of("code", 200, "data", badgeService.getMyBadges(userId));
    }

    // 管理员创建 (用于初始化测试数据)
    @PostMapping("/web/badges")
    public Map<String, Object> createBadge(@RequestBody Badge badge) {
        return Map.of("code", 200, "data", badgeService.createBadge(badge));
    }
}