package com.example.EcoGo.service;

import com.example.EcoGo.dto.PointsDto;
import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.PointsService;
import com.example.EcoGo.model.User;
import com.example.EcoGo.model.UserPointsLog;
import com.example.EcoGo.repository.UserPointsLogRepository;
import com.example.EcoGo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PointsServiceImpl implements PointsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPointsLogRepository pointsLogRepository;

    @Override
    public UserPointsLog adjustPoints(String userId, long points, String source, String description,
            UserPointsLog.AdminAction adminAction) {
        // 1. Fetch User (using UUID)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. Validate sufficient funds for deduction
        long newBalance = user.getCurrentPoints() + points;
        if (newBalance < 0) {
            // Usually we don't allow negative balance
            throw new BusinessException(ErrorCode.PARAM_ERROR, "Insufficient points");
        }

        // 3. Update User
        user.setCurrentPoints(newBalance);
        if (points > 0) {
            user.setTotalPoints(user.getTotalPoints() + points);
        }
        userRepository.save(user);

        // 4. Create Log
        String changeType = points > 0 ? "gain" : (points < 0 ? "deduct" : "info");
        // If source is REDEEM, type might be redeem
        if ("redeem".equalsIgnoreCase(source)) {
            changeType = "redeem";
        }

        UserPointsLog log = new UserPointsLog();
        log.setUserId(userId);
        log.setChangeType(changeType);
        log.setPoints(points);
        log.setSource(source);
        log.setAdminAction(adminAction);
        log.setBalanceAfter(newBalance);
        // relatedId ? left null for now, can be added to method signature if needed
        // later

        return pointsLogRepository.save(log);
    }

    @Override
    public PointsDto.CurrentPointsResponse getCurrentPoints(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new PointsDto.CurrentPointsResponse(user.getUserid(), user.getCurrentPoints(), user.getTotalPoints());
    }

    @Autowired
    private com.example.EcoGo.repository.TransportModeRepository transportModeRepository;

    @Override
    public long calculatePoints(String mode, double distance) {
        // 1. Fetch Mode
        var transportMode = transportModeRepository.findByMode(mode)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "Invalid transport mode: " + mode));

        // 2. Constants (Should match DB)
        double carCarbon = 100.0; // g/km (Benchmark)

        // 3. Calc Saving: (Car - Current) * Distance
        double savingPerKm = carCarbon - transportMode.getCarbonFactor();
        if (savingPerKm < 0)
            savingPerKm = 0; // Should not punish? Or maybe negative points? Assuming non-negative reward.

        double totalCarbonSaved = savingPerKm * distance;

        // 4. Points: 1g = 10 pts
        return (long) (totalCarbonSaved * 10);
    }

    @Override
    public PointsDto.TripStatsResponse getTripStats(String userId) {
        List<UserPointsLog> logs;
        if (userId == null) {
            // Global stats
            logs = pointsLogRepository.findBySource("trip");
        } else {
            // User stats
            logs = pointsLogRepository.findByUserIdAndSource(userId, "trip");
        }

        long totalTrips = logs.size();
        long totalPoints = logs.stream().mapToLong(UserPointsLog::getPoints).sum();

        return new PointsDto.TripStatsResponse(totalTrips, totalPoints);
    }

    @Override
    public List<PointsDto.CurrentPointsResponse> getAllUserPoints() {
        return userRepository.findAll().stream()
                .map(user -> new PointsDto.CurrentPointsResponse(user.getUserid(), user.getCurrentPoints(),
                        user.getTotalPoints()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PointsDto.PointsLogResponse> getAllPointsHistory() {
        return pointsLogRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void settleTrip(String userId, String tripId, double carbonAmount) {
        // For settled trips, carbon is already calculated by ML.
        // We just convert Carbon -> Points (1g = 10pts)
        long points = (long) (carbonAmount * 10);

        String description = "Trip completed: " + tripId;
        // Reuse adjustPoints logic
        adjustPoints(userId, points, "trip", description, null);
    }

    @Override
    public void redeemPoints(String userId, String orderId, long points) {
        String description = "Redemption for order: " + orderId;
        // Points should be negative for deduction
        adjustPoints(userId, -Math.abs(points), "redeem", description, null);
    }

    @Override
    public void refundPoints(String userId, String orderId) {
        // This is complex. Ideally we find the original deduction log.
        // For MVP, we'll just add points back with source="refund"
        // In a real system, we'd look up the transaction amount.
        // Assuming the caller knows the amount, OR we just support manual admin refund
        // now.
        // Wait, the interface didn't pass amount. Let's find the log by
        // relatedId=orderId?
        // Simpler approach for now: Refund implies reversing a specific transaction.
        // But let's assume this method is a placeholder for logical flow.
        // Since I can't easily find the original amount without relatedId query support
        // (which I didn't add yet for arbitrary lookup),
        // I will throw exception or just log for now?
        // Better: Let's assume the caller passes amount? The interface didn't have
        // amount.
        // Let's modify the interface or just use a dummy implementation for now.
        // Actually, I can use a rudimentary lookup if I added relatedID lookup.
        // Let's just implement a stub that throws "Not Implemented" for now or fix
        // interface.
        // User asked for "Refund Points for Order Cancellation", usually order service
        // knows amount.
        // I will change interface to accept amount in next step if needed, but for now
        // let's just make it a no-op or specific error.
        throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                "Refund logic requires amount parameter or transaction lookup");
    }

    // Helper to avoid duplication
    private PointsDto.PointsLogResponse convertToDto(UserPointsLog log) {
        PointsDto.PointsLogResponse dto = new PointsDto.PointsLogResponse();
        dto.id = log.getId();
        dto.change_type = log.getChangeType();
        dto.points = log.getPoints();
        dto.source = log.getSource();
        dto.balance_after = log.getBalanceAfter();
        dto.created_at = log.getCreatedAt().toString();

        if (log.getAdminAction() != null) {
            PointsDto.AdminActionDto adminDto = new PointsDto.AdminActionDto();
            adminDto.operator_id = log.getAdminAction().getOperatorId();
            adminDto.reason = log.getAdminAction().getReason();
            adminDto.approval_status = log.getAdminAction().getApprovalStatus();
            dto.admin_action = adminDto;
        }
        return dto;
    }

    @Override
    public List<PointsDto.PointsLogResponse> getPointsHistory(String userId) {
        List<UserPointsLog> logs = pointsLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return logs.stream().map(this::convertToDto).collect(Collectors.toList());
    }
}
