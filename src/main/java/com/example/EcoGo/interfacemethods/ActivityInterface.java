package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.Activity;

import java.util.List;

public interface ActivityInterface {
    // CRUD
    List<Activity> getAllActivities();
    Activity getActivityById(String id);
    Activity createActivity(Activity activity);
    Activity updateActivity(String id, Activity activity);
    void deleteActivity(String id);

    // 业务方法
    List<Activity> getActivitiesByStatus(String status);
    Activity publishActivity(String id);
    Activity joinActivity(String activityId, String userId);
    Activity leaveActivity(String activityId, String userId);
}
