package com.example.EcoGo.controller;

import com.example.EcoGo.dto.PointsDto;
import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.PointsService;
import com.example.EcoGo.model.User;
import com.example.EcoGo.model.UserPointsLog;
import com.example.EcoGo.repository.UserRepository;
import com.example.EcoGo.utils.JwtUtils; // Fixed import package name
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private UserRepository userRepository; // Need to resolve business ID for admin

    @Autowired
    private JwtUtils jwtUtils;

    // --- Mobile Endpoints ---

    /**
     * Get Current Points Balance
     * GET /api/v1/mobile/points/current
     */
    @GetMapping("/api/v1/mobile/points/current")
    public ResponseMessage<PointsDto.CurrentPointsResponse> getCurrentPoints(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtils.getUserIdFromToken(token); // UUID
        return ResponseMessage.success(pointsService.getCurrentPoints(userId));
    }

    /**
     * Get Points History
     * GET /api/v1/mobile/points/history
     */
    @GetMapping("/api/v1/mobile/points/history")
    public ResponseMessage<List<PointsDto.PointsLogResponse>> getHistory(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtils.getUserIdFromToken(token); // UUID
        return ResponseMessage.success(pointsService.getPointsHistory(userId));
    }

    // --- Web / Admin Endpoints ---

    /**
     * Admin Adjust Points
     * POST /api/v1/web/points/adjust
     * Body: { "target_userid": "123", "points": 100, "source": "admin", "reason":
     * "bonus" }
     */
    @PostMapping("/api/v1/web/points/adjust")
    public ResponseMessage<String> adjustPointsAdmin(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AdminAdjustRequest request) {

        // 1. Resolve Target User Business ID -> UUID
        User targetUser = userRepository.findByUserid(request.target_userid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. Prepare Admin Action Info
        UserPointsLog.AdminAction adminAction = new UserPointsLog.AdminAction();
        // Extract operator ID from token if needed, or just use "admin"
        String token = authHeader.replace("Bearer ", "");
        String operatorId = jwtUtils.getUserIdFromToken(token);
        adminAction.setOperatorId(operatorId);
        adminAction.setReason(request.reason);
        adminAction.setApprovalStatus("approved");

        // 3. Call Service
        pointsService.adjustPoints(targetUser.getId(), request.points, request.source, request.reason, adminAction);

        return ResponseMessage.success("Points adjusted successfully");
    }

    // Helper DTO for Admin Request
    public static class AdminAdjustRequest {
        public String target_userid; // Business ID
        public long points;
        public String source; // "admin"
        public String reason;
    }
}
