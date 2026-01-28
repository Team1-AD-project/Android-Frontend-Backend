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

    @Override
    public List<PointsDto.PointsLogResponse> getPointsHistory(String userId) {
        List<UserPointsLog> logs = pointsLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return logs.stream().map(log -> {
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
        }).collect(Collectors.toList());
    }
}
