package com.example.EcoGo.config;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.EcoGo.model.User;
import com.example.EcoGo.repository.UserRepository;
import com.example.EcoGo.repository.BusRouteRepository;
import com.example.EcoGo.repository.WalkingRouteRepository;
import com.example.EcoGo.repository.FacultyRepository;
import com.example.EcoGo.repository.VoucherRepository;
import com.example.EcoGo.repository.HistoryRepository;
import com.example.EcoGo.utils.PasswordUtils;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordUtils passwordUtils;
    private final com.example.EcoGo.repository.TransportModeRepository transportModeRepository;
    private final BusRouteRepository busRouteRepository;
    private final WalkingRouteRepository walkingRouteRepository;
    private final FacultyRepository facultyRepository;
    private final VoucherRepository voucherRepository;
    private final HistoryRepository historyRepository;

    public DatabaseSeeder(UserRepository userRepository, PasswordUtils passwordUtils,
            com.example.EcoGo.repository.TransportModeRepository transportModeRepository,
            BusRouteRepository busRouteRepository,
            WalkingRouteRepository walkingRouteRepository,
            FacultyRepository facultyRepository,
            VoucherRepository voucherRepository,
            HistoryRepository historyRepository) {
        this.userRepository = userRepository;
        this.passwordUtils = passwordUtils;
        this.transportModeRepository = transportModeRepository;
        this.busRouteRepository = busRouteRepository;
        this.walkingRouteRepository = walkingRouteRepository;
        this.facultyRepository = facultyRepository;
        this.voucherRepository = voucherRepository;
        this.historyRepository = historyRepository;
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

        // --- Seed Transport Modes ---
        if (transportModeRepository.count() == 0) {
            seedTransportModes();
            System.out.println("Transport modes seeded.");
        }

        if (busRouteRepository.count() == 0) {
            seedBusRoutes();
        }
        if (walkingRouteRepository.count() == 0) {
            seedWalkingRoutes();
        }
        if (facultyRepository.count() == 0) {
            seedFaculties();
        }
        if (voucherRepository.count() == 0) {
            seedVouchers();
        }
        if (historyRepository.count() == 0) {
            seedHistory();
        }
    }

    private void seedTransportModes() {
        java.util.List<com.example.EcoGo.model.TransportMode> modes = java.util.Arrays.asList(
                new com.example.EcoGo.model.TransportMode("1001", "walk", "步行", 0, "https://xxx/icon/walk.png", 1,
                        true),
                new com.example.EcoGo.model.TransportMode("1002", "bike", "自行车", 0, "https://xxx/icon/bike.png", 2,
                        true),
                new com.example.EcoGo.model.TransportMode("1003", "bus", "公交", 20, "https://xxx/icon/bus.png", 3, true),
                new com.example.EcoGo.model.TransportMode("1004", "subway", "地铁", 10, "https://xxx/icon/subway.png", 4,
                        true),
                new com.example.EcoGo.model.TransportMode("1005", "car", "私家车", 100, "https://xxx/icon/car.png", 5,
                        false), // Not green
                new com.example.EcoGo.model.TransportMode("1006", "electric_bike", "电动车", 5,
                        "https://xxx/icon/ebike.png", 6, true));
        transportModeRepository.saveAll(modes);
    }

    private void seedBusRoutes() {
        java.util.List<com.example.EcoGo.model.BusRoute> routes = java.util.Arrays.asList(
                new com.example.EcoGo.model.BusRoute("D1", "D1", "Opp Hon Sui Sen", "UTown", "#DB2777", "Arriving", "2 min", "Low"),
                new com.example.EcoGo.model.BusRoute("D2", "D2", "Car Park 11", "UTown", "#7C3AED", "On Time", "5 min", "Med"),
                new com.example.EcoGo.model.BusRoute("A1", "A1", "PGP Terminal", "Kent Ridge MRT", "#DC2626", "Delayed", "12 min", "High"),
                new com.example.EcoGo.model.BusRoute("A2", "A2", "Kent Ridge MRT", "PGP Terminal", "#F59E0B", "Arriving", "3 min", "Low"),
                new com.example.EcoGo.model.BusRoute("BTC", "BTC", "Kent Ridge", "Bukit Timah", "#059669", "Scheduled", "25 min", "Low"));
        busRouteRepository.saveAll(routes);
    }

    private void seedWalkingRoutes() {
        java.util.List<com.example.EcoGo.model.WalkingRoute> routes = java.util.Arrays.asList(
                new com.example.EcoGo.model.WalkingRoute("1", "Kent Ridge Forest Walk", "15 min", "1.2 km", "85 kcal",
                        java.util.Arrays.asList("Nature", "Hilly"),
                        "A scenic route through the secondary forest connecting Science Park and Kent Ridge."),
                new com.example.EcoGo.model.WalkingRoute("2", "UTown to Science", "10 min", "0.8 km", "50 kcal",
                        java.util.Arrays.asList("Sheltered", "Easy"),
                        "The quickest sheltered linkway avoiding the internal shuttle bus crowds."),
                new com.example.EcoGo.model.WalkingRoute("3", "FASS Ridge Hike", "20 min", "1.5 km", "110 kcal",
                        java.util.Arrays.asList("Workout", "View"),
                        "Challenge yourself with the stairs up the ridge and enjoy the view."));
        walkingRouteRepository.saveAll(routes);
    }

    private void seedFaculties() {
        java.util.List<com.example.EcoGo.model.Faculty> faculties = java.util.Arrays.asList(
                new com.example.EcoGo.model.Faculty("utown", "UTown", "#10b981", "city", 3850, 3),
                new com.example.EcoGo.model.Faculty("engin", "Engin", "#f97316", "gear", 4105, 2),
                new com.example.EcoGo.model.Faculty("soc", "SoC", "#3b82f6", "laptop", 4520, 1),
                new com.example.EcoGo.model.Faculty("science", "Science", "#8b5cf6", "flask", 2800, 6),
                new com.example.EcoGo.model.Faculty("fass", "FASS", "#eab308", "book", 3100, 4),
                new com.example.EcoGo.model.Faculty("biz", "Biz", "#ec4899", "chart", 3200, 4));
        facultyRepository.saveAll(faculties);
    }

    private void seedVouchers() {
        java.util.List<com.example.EcoGo.model.Voucher> vouchers = java.util.Arrays.asList(
                new com.example.EcoGo.model.Voucher("sbux_10", "$10 Starbucks", 1000, "#00704A", "coffee", "Valid at UTown & Frontier"),
                new com.example.EcoGo.model.Voucher("subway_5", "$5 Subway", 500, "#FFC72C", "sandwich", "Valid at Yusof Ishak House"),
                new com.example.EcoGo.model.Voucher("nus_2", "$2 Canteen Voucher", 200, "#F97316", "bowl", "Valid at The Deck & Techno"),
                new com.example.EcoGo.model.Voucher("liho_3", "$3 LiHO Tea", 350, "#DC2626", "tea", "Valid at Central Library"));
        voucherRepository.saveAll(vouchers);
    }

    private void seedHistory() {
        java.util.List<com.example.EcoGo.model.HistoryItem> items = java.util.Arrays.asList(
                new com.example.EcoGo.model.HistoryItem("1", "Bus D1 to UTown", "10:30 AM", "+50", "earn"),
                new com.example.EcoGo.model.HistoryItem("2", "Walked to Library", "Yesterday", "+30", "earn"),
                new com.example.EcoGo.model.HistoryItem("3", "Bought Orange Cap", "Yesterday", "-200", "spend"),
                new com.example.EcoGo.model.HistoryItem("4", "Daily Check-in", "Yesterday", "+10", "earn"),
                new com.example.EcoGo.model.HistoryItem("5", "Bus A2 to PGP", "2 days ago", "+50", "earn"));
        historyRepository.saveAll(items);
    }
}
