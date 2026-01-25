package com.example.EcoGo.service;

import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.ActivityInterface;
import com.example.EcoGo.model.Activity;
import com.example.EcoGo.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ActivityImplementation implements ActivityInterface {

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    @Override
    public Activity getActivityById(String id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
    }

    @Override
    public Activity createActivity(Activity activity) {
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        if (activity.getStatus() == null) activity.setStatus("DRAFT");
        if (activity.getCurrentParticipants() == null) activity.setCurrentParticipants(0);
        if (activity.getParticipantIds() == null) activity.setParticipantIds(new ArrayList<>());
        return activityRepository.save(activity);
    }

    @Override
    public Activity updateActivity(String id, Activity activity) {
        Activity existing = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        if (activity.getTitle() != null) existing.setTitle(activity.getTitle());
        if (activity.getDescription() != null) existing.setDescription(activity.getDescription());
        if (activity.getType() != null) existing.setType(activity.getType());
        if (activity.getStatus() != null) existing.setStatus(activity.getStatus());
        if (activity.getRewardCredits() != null) existing.setRewardCredits(activity.getRewardCredits());
        if (activity.getMaxParticipants() != null) existing.setMaxParticipants(activity.getMaxParticipants());
        if (activity.getStartTime() != null) existing.setStartTime(activity.getStartTime());
        if (activity.getEndTime() != null) existing.setEndTime(activity.getEndTime());
        existing.setUpdatedAt(LocalDateTime.now());

        return activityRepository.save(existing);
    }

    @Override
    public void deleteActivity(String id) {
        if (!activityRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        activityRepository.deleteById(id);
    }

    @Override
    public List<Activity> getActivitiesByStatus(String status) {
        return activityRepository.findByStatus(status);
    }

    @Override
    public Activity publishActivity(String id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
        activity.setStatus("PUBLISHED");
        activity.setUpdatedAt(LocalDateTime.now());
        return activityRepository.save(activity);
    }

    @Override
    public Activity joinActivity(String activityId, String userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        // 检查是否已满
        if (activity.getMaxParticipants() != null &&
                activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.ACTIVITY_FULL);
        }

        // 检查是否已参加
        if (activity.getParticipantIds() == null) {
            activity.setParticipantIds(new ArrayList<>());
        }
        if (activity.getParticipantIds().contains(userId)) {
            throw new BusinessException(ErrorCode.ALREADY_JOINED);
        }

        activity.getParticipantIds().add(userId);
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activity.setUpdatedAt(LocalDateTime.now());

        return activityRepository.save(activity);
    }

    @Override
    public Activity leaveActivity(String activityId, String userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));

        if (activity.getParticipantIds() != null && activity.getParticipantIds().contains(userId)) {
            activity.getParticipantIds().remove(userId);
            activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
            activity.setUpdatedAt(LocalDateTime.now());
        }

        return activityRepository.save(activity);
    }
}
