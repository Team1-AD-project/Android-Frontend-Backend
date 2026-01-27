package com.example.EcoGo.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.EcoGo.dto.AuthDto;
import com.example.EcoGo.dto.UserProfileDto;
import com.example.EcoGo.dto.UserResponseDto;
import com.example.EcoGo.exception.BusinessException;
import com.example.EcoGo.exception.errorcode.ErrorCode;
import com.example.EcoGo.interfacemethods.UserInterface;
import com.example.EcoGo.model.User;
import com.example.EcoGo.repository.UserRepository;
import com.example.EcoGo.utils.JwtUtils;
import com.example.EcoGo.utils.PasswordUtils;

@Service
public class UserServiceImpl implements UserInterface {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordUtils passwordUtils;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository, PasswordUtils passwordUtils, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordUtils = passwordUtils;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public UserResponseDto getUserByUsername(String username) {
        // Keeping existing method for compatibility
        return userRepository.findByUserid(username)
                .map(u -> new UserResponseDto(u.getEmail(), u.getUserid(), u.getNickname(), u.getPhone())) // Adapt to
                                                                                                           // existing
                                                                                                           // DTO
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // --- Mobile Auth ---

    @Override
    public AuthDto.RegisterResponse register(AuthDto.MobileRegisterRequest request) {
        if (request.email == null || !request.email.endsWith("@nus.edu.cn")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅支持NUS邮箱注册 (@nus.edu.cn)");
        }

        if (userRepository.findByEmail(request.email).isPresent()) {
            throw new BusinessException(ErrorCode.USER_NAME_DUPLICATE, "邮箱已存在");
        }

        // Generate userid from email prefix (e.g., abc@test.com -> abc)
        String userid = request.email.split("@")[0];
        // Ensure userid is unique (simple logic: if exists, append random suffix)
        if (userRepository.findByUserid(userid).isPresent()) {
            userid = userid + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUserid(userid);
        user.setEmail(request.email);
        // user.setPhone(request.phone); // Removed in this flow
        user.setPassword(passwordUtils.encode(request.password));
        user.setNickname(request.nickname);

        // Set Default Preferences (User doesn't provide them at registration)
        User.Preferences preferences = new User.Preferences();
        preferences.setPreferredTransport("bus");
        preferences.setEnablePush(true);
        preferences.setEnableEmail(true); // Default to true since they registered with email
        preferences.setEnableBusReminder(true);
        preferences.setLanguage("zh");
        preferences.setTheme("light");
        preferences.setShareLocation(true);
        preferences.setShowOnLeaderboard(true);
        preferences.setShareAchievements(true);
        user.setPreferences(preferences);

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Defaults
        user.setTotalPoints(0);
        user.setCurrentPoints(0);
        user.setTotalCarbon(0);

        User.Vip vip = new User.Vip();
        vip.setActive(false);
        user.setVip(vip);

        User.Stats stats = new User.Stats();
        // Initialize stats with defaults (0)
        stats.setTotalTrips(0);
        stats.setTotalDistance(0.0);
        stats.setGreenDays(0);
        stats.setWeeklyRank(0);
        stats.setMonthlyRank(0);
        user.setStats(stats);

        userRepository.save(user);
        logger.info("User registered: {}", user.getId());

        return new AuthDto.RegisterResponse(user);
    }

    @Override
    public AuthDto.LoginResponse loginMobile(AuthDto.MobileLoginRequest request) {
        // Login by UserID
        User user = userRepository.findByUserid(request.userid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户ID不存在"));

        if (!passwordUtils.matches(request.password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getId(), user.isAdmin());
        String expireAt = jwtUtils.getExpirationDate(token).toString();

        return new AuthDto.LoginResponse(token, expireAt, user);
    }

    @Override
    public void logoutMobile(String token, String userId) {
        // Implementation depends on Token blacklist strategy (Redis)
        // For now, stateless JWT, client side discard
        logger.info("User logout: {}", userId);
    }

    // --- Mobile Profile ---

    @Override
    public UserProfileDto.UpdateProfileResponse updateProfile(UserProfileDto.UpdateProfileRequest request) {
        User user = userRepository.findById(request.user_id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.nickname != null)
            user.setNickname(request.nickname);
        if (request.avatar != null)
            user.setAvatar(request.avatar);
        if (request.preferences != null)
            user.setPreferences(request.preferences);

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new UserProfileDto.UpdateProfileResponse(user.getId(), user.getUpdatedAt());
    }

    @Override
    public UserProfileDto.PreferencesResetResponse resetPreferences(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Reset to default logic here
        User.Preferences defaultPref = new User.Preferences();
        defaultPref.setLanguage("zh");
        defaultPref.setTheme("light");
        // ... set other defaults

        user.setPreferences(defaultPref);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new UserProfileDto.PreferencesResetResponse(defaultPref, user.getUpdatedAt());
    }

    // --- Web Auth ---

    @Override
    public AuthDto.LoginResponse loginWeb(AuthDto.WebLoginRequest request) {
        // Allow login by Username or Phone
        User user = userRepository.findByUserid(request.userid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordUtils.matches(request.password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        if (!user.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "非管理员账号");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getId(), user.isAdmin());
        String expireAt = jwtUtils.getExpirationDate(token).toString();

        return new AuthDto.LoginResponse(token, expireAt, user);
    }

    @Override
    public void logoutWeb(String token) {
        logger.info("Web admin logout");
    }

    // --- Web Admin ---

    @Override
    public UserProfileDto.AuthCheckResponse authenticateUser(String token) {
        // In real filter chain, this is done before controller
        // Here just simulating extraction
        try {
            var claims = jwtUtils.validateToken(token);
            boolean isAdmin = (boolean) claims.get("isAdmin");
            // Mock permissions
            return new UserProfileDto.AuthCheckResponse(isAdmin, Collections.singletonList("ALL"));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "Invalid Token");
        }
    }

    @Override
    public boolean authorizeUser(String token, String permission) {
        try {
            var claims = jwtUtils.validateToken(token);
            return (boolean) claims.get("isAdmin");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public UserProfileDto.UpdateProfileResponse manageUser(String userId,
            UserProfileDto.AdminManageUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setAdmin(request.isAdmin);
        if (user.getVip() == null)
            user.setVip(new User.Vip());
        user.getVip().setActive(request.vip_status);
        // Remark not in model yet, ignoring

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new UserProfileDto.UpdateProfileResponse(user.getId(), user.getUpdatedAt());
    }

    @Override
    public UserProfileDto.UserDetailResponse getUserDetail(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new UserProfileDto.UserDetailResponse(user);
    }

    @Override
    public UserProfileDto.UpdateProfileResponse updateProfileAdmin(String userId,
            UserProfileDto.UpdateProfileRequest request) {
        return updateProfile(request); // Reuse logic
    }
}
