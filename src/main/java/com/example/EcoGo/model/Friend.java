package com.example.EcoGo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * 好友关系实体
 * 用户好友关系管理
 */
@Document(collection = "friends")
public class Friend {
    @Id
    private String id;
    private String userId; // 用户ID
    private String friendId; // 好友用户ID
    private String friendNickname; // 好友昵称
    private String friendAvatarUrl; // 好友头像URL
    private String friendFaculty; // 好友学院
    private Integer friendPoints; // 好友积分
    private Integer friendRank; // 好友排名
    private String status; // 好友状态：pending(待确认), accepted(已接受), blocked(已屏蔽)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Friend() {
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Friend(String userId, String friendId) {
        this();
        this.userId = userId;
        this.friendId = friendId;
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

    public String getFriendNickname() {
        return friendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
    }

    public String getFriendAvatarUrl() {
        return friendAvatarUrl;
    }

    public void setFriendAvatarUrl(String friendAvatarUrl) {
        this.friendAvatarUrl = friendAvatarUrl;
    }

    public String getFriendFaculty() {
        return friendFaculty;
    }

    public void setFriendFaculty(String friendFaculty) {
        this.friendFaculty = friendFaculty;
    }

    public Integer getFriendPoints() {
        return friendPoints;
    }

    public void setFriendPoints(Integer friendPoints) {
        this.friendPoints = friendPoints;
    }

    public Integer getFriendRank() {
        return friendRank;
    }

    public void setFriendRank(Integer friendRank) {
        this.friendRank = friendRank;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
