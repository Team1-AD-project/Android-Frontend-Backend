package com.example.EcoGo.dto;

import com.example.EcoGo.model.User;
import java.time.LocalDateTime;
import java.util.List;

public class UserProfileDto {

    public static class UpdateProfileRequest {

        public String nickname;
        public String avatar;
        public String phone;
        public User.Preferences preferences;
    }

    public static class AdminManageUserRequest {
        public boolean isAdmin;
        public boolean vip_status;
        public String remark;
    }

    public static class UserDetailResponse {
        public User user_info;
        public User.Vip vip_info;
        public User.Stats stats;

        public UserDetailResponse(User user) {
            this.user_info = user;
            this.vip_info = user.getVip();
            this.stats = user.getStats();
        }
    }

    public static class UpdateProfileResponse {
        public String user_id;
        public String updated_at;

        public UpdateProfileResponse(String id, LocalDateTime time) {
            this.user_id = id;
            this.updated_at = time.toString();
        }
    }

    public static class AuthCheckResponse {
        public boolean is_admin;
        public List<String> permissions;

        public AuthCheckResponse(boolean isAdmin, List<String> permissions) {
            this.is_admin = isAdmin;
            this.permissions = permissions;
        }
    }

    public static class PreferencesResetResponse {
        public User.Preferences preferences;
        public String updated_at;

        public PreferencesResetResponse(User.Preferences pref, LocalDateTime time) {
            this.preferences = pref;
            this.updated_at = time.toString();
        }
    }
}
