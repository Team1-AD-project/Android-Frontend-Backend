package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * 好友动态实体
 * 记录好友的活动动态
 */
@Document(collection = "friend_activities")
public class FriendActivity {
    @Id
    private String id;
    private String userId; // 用户ID
    private String friendId; // 好友ID
    private String friendName; // 好友名称
    private String action; // 动作类型：joined_activity, earned_badge, completed_goal, etc.
    private String details; // 详细信息
    private LocalDateTime timestamp; // 时间戳

    public FriendActivity() {
        this.timestamp = LocalDateTime.now();
    }

    public FriendActivity(String userId, String friendId, String friendName, String action, String details) {
        this();
        this.userId = userId;
        this.friendId = friendId;
        this.friendName = friendName;
        this.action = action;
        this.details = details;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
