package com.example.EcoGo.dto;

import com.example.EcoGo.model.User;
import jakarta.validation.constraints.NotBlank;

public class AuthDto {

    public static class MobileRegisterRequest {
        // @NotBlank(message = "用户ID不能为空")
        // public String userid;
        // @NotBlank(message = "手机号不能为空")
        // public String phone;
        @NotBlank(message = "邮箱不能为空")
        public String email;
        @NotBlank(message = "昵称不能为空")
        public String nickname;
        @NotBlank(message = "密码不能为空")
        public String password;
    }

    public static class MobileLoginRequest {
        // @NotBlank(message = "手机号不能为空")
        // public String phone;
        @NotBlank(message = "用户ID不能为空")
        public String userid;
        @NotBlank(message = "密码不能为空")
        public String password;
    }

    public static class WebLoginRequest {
        // @NotBlank(message = "用户名/手机号不能为空")
        // public String username;
        @NotBlank(message = "用户ID不能为空")
        public String userid;
        @NotBlank(message = "密码不能为空")
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public String expire_at;
        public UserInfo user_info;
        public Object admin_info;

        public LoginResponse(String token, String expire_at, User user) {
            this.token = token;
            this.expire_at = expire_at;
            this.user_info = new UserInfo(user);
        }
    }

    public static class UserInfo {
        public String id;
        public String userid;
        public String nickname;
        public boolean isAdmin;
        public User.Vip vip;

        public UserInfo(User user) {
            this.id = user.getId();
            this.userid = user.getUserid();
            this.nickname = user.getNickname();
            this.isAdmin = user.isAdmin();
            this.vip = user.getVip();
        }
    }

    public static class RegisterResponse {
        public String id;
        public String userid;
        public String nickname;
        public String created_at;

        public RegisterResponse(User user) {
            this.id = user.getId();
            this.userid = user.getUserid();
            this.nickname = user.getNickname();
            this.created_at = user.getCreatedAt().toString();
        }
    }
}
