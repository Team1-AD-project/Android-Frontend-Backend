package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.dto.AuthDto;
import com.example.EcoGo.dto.UserProfileDto;
import com.example.EcoGo.dto.UserResponseDto;

public interface UserInterface {

    // Existing method
    UserResponseDto getUserByUsername(String username);

    // Mobile - Auth
    AuthDto.RegisterResponse register(AuthDto.MobileRegisterRequest request);

    AuthDto.LoginResponse loginMobile(AuthDto.MobileLoginRequest request);

    void logoutMobile(String token, String userId);

    // Mobile - Profile
    UserProfileDto.UpdateProfileResponse updateProfile(String userId, UserProfileDto.UpdateProfileRequest request);

    UserProfileDto.PreferencesResetResponse resetPreferences(String userId);

    // Web - Auth
    AuthDto.LoginResponse loginWeb(AuthDto.WebLoginRequest request);

    void logoutWeb(String token);

    // Web - Admin
    UserProfileDto.AuthCheckResponse authenticateUser(String token);

    boolean authorizeUser(String token, String permission);

    UserProfileDto.UpdateProfileResponse manageUser(String userId, UserProfileDto.AdminManageUserRequest request);

    UserProfileDto.UserDetailResponse getUserDetail(String userId);

    UserProfileDto.UpdateProfileResponse updateProfileAdmin(String userId, UserProfileDto.UpdateProfileRequest request);
}
