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
    UserProfileDto.UpdateProfileResponse updateProfile(String token, String userId,
            UserProfileDto.UpdateProfileRequest request);

    UserProfileDto.PreferencesResetResponse resetPreferences(String token, String userId);

    void deleteUser(String token, String userId);

    // Web - Auth
    AuthDto.LoginResponse loginWeb(AuthDto.WebLoginRequest request);

    void logoutWeb(String token);

    // Web - Admin
    UserProfileDto.AuthCheckResponse authenticateUser(String token);

    UserProfileDto.UpdateProfileResponse manageUser(String userId, UserProfileDto.AdminManageUserRequest request);

    UserProfileDto.UserDetailResponse getUserDetail(String token, String userId);

    java.util.List<UserResponseDto> getAllUsers(); // New Admin List

    UserProfileDto.UpdateProfileResponse updateProfileAdmin(String userId, UserProfileDto.UpdateProfileRequest request);
}
