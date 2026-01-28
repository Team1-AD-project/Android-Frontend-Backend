package com.example.EcoGo.config;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.EcoGo.model.User;
import com.example.EcoGo.repository.UserRepository;
import com.example.EcoGo.utils.PasswordUtils;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordUtils passwordUtils;

    public DatabaseSeeder(UserRepository userRepository, PasswordUtils passwordUtils) {
        this.userRepository = userRepository;
        this.passwordUtils = passwordUtils;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUserid("admin").isEmpty()) {
            User admin = new User();
            admin.setId(UUID.randomUUID().toString());
            admin.setUserid("admin");
            admin.setUsername("Super Admin");
            admin.setEmail("admin@eco.go");
            admin.setPassword(passwordUtils.encode("admin123")); // Default password
            admin.setNickname("System Admin");
            admin.setAdmin(true);

            // Initialize defaults
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            admin.setLastLoginAt(LocalDateTime.now());

            // Preferences
            User.Preferences prefs = new User.Preferences();
            prefs.setLanguage("en");
            prefs.setTheme("dark");
            admin.setPreferences(prefs);

            // Vip
            User.Vip vip = new User.Vip();
            vip.setActive(true);
            vip.setPlan("YEARLY");
            vip.setExpiryDate(LocalDateTime.now().plusYears(99));
            admin.setVip(vip);

            // Stats
            User.Stats stats = new User.Stats();
            admin.setStats(stats);

            // ActivityMetrics
            User.ActivityMetrics metrics = new User.ActivityMetrics();
            metrics.setLoginDates(new java.util.ArrayList<>());
            admin.setActivityMetrics(metrics);

            userRepository.save(admin);
            System.out.println("---------------------------------------------");
            System.out.println("Admin user created: userid=admin, password=admin123");
            System.out.println("---------------------------------------------");
        }
    }
}
