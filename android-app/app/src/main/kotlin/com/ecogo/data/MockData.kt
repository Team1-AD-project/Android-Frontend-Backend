package com.ecogo.data

object MockData {
    
    // === Navigation Related Mock Data ===
    
    /**
     * Campus Location Data
     * 使用 lazy 延迟初始化，避免启动时加载
     */
    val CAMPUS_LOCATIONS by lazy { listOf(
        NavLocation(
            id = "1",
            name = "COM1",
            address = "School of Computing",
            latitude = 1.2948,
            longitude = 103.7743,
            type = LocationType.FACULTY,
            icon = "💻",
            isFavorite = true,
            visitCount = 15
        ),
        NavLocation(
            id = "2",
            name = "UTown",
            address = "University Town",
            latitude = 1.3036,
            longitude = 103.7739,
            type = LocationType.FACILITY,
            icon = "🏙️",
            isFavorite = true,
            visitCount = 20
        ),
        NavLocation(
            id = "3",
            name = "Central Library",
            address = "Main Library",
            latitude = 1.2966,
            longitude = 103.7723,
            type = LocationType.LIBRARY,
            icon = "📚",
            isFavorite = false,
            visitCount = 8
        ),
        NavLocation(
            id = "4",
            name = "PGP Residence",
            address = "Prince George's Park",
            latitude = 1.2913,
            longitude = 103.7803,
            type = LocationType.RESIDENCE,
            icon = "🏠",
            isFavorite = true,
            visitCount = 30
        ),
        NavLocation(
            id = "5",
            name = "The Deck",
            address = "Faculty of Engineering",
            latitude = 1.2993,
            longitude = 103.7710,
            type = LocationType.CANTEEN,
            icon = "🍜",
            isFavorite = false,
            visitCount = 12
        ),
        NavLocation(
            id = "6",
            name = "Kent Ridge MRT",
            address = "MRT Station",
            latitude = 1.2931,
            longitude = 103.7843,
            type = LocationType.BUS_STOP,
            icon = "🚇",
            isFavorite = false,
            visitCount = 5
        ),
        NavLocation(
            id = "7",
            name = "Business School",
            address = "NUS Business School",
            latitude = 1.2935,
            longitude = 103.7751,
            type = LocationType.FACULTY,
            icon = "📈",
            isFavorite = false,
            visitCount = 3
        ),
        NavLocation(
            id = "8",
            name = "Science Park",
            address = "Science Faculty",
            latitude = 1.2958,
            longitude = 103.7807,
            type = LocationType.FACULTY,
            icon = "🧪",
            isFavorite = false,
            visitCount = 7
        ),
        NavLocation(
            id = "9",
            name = "Sports Hall",
            address = "MPSH",
            latitude = 1.3010,
            longitude = 103.7765,
            type = LocationType.FACILITY,
            icon = "⚽",
            isFavorite = false,
            visitCount = 4
        ),
        NavLocation(
            id = "10",
            name = "Yusof Ishak House",
            address = "YIH",
            latitude = 1.2979,
            longitude = 103.7757,
            type = LocationType.FACILITY,
            icon = "🏢",
            isFavorite = false,
            visitCount = 6
        )
    ) }
    
    /**
     * Sample Bus Information
     * 使用 lazy 延迟初始化
     */
    val BUS_INFO_LIST by lazy { listOf(
        BusInfo(
            busId = "D1",
            routeName = "D1",
            destination = "UTown",
            currentLat = 1.2950,
            currentLng = 103.7750,
            etaMinutes = 2,
            stopsAway = 3,
            crowdLevel = "Low",
            plateNumber = "SBS3421A",
            status = "arriving",
            color = "#DB2777"
        ),
        BusInfo(
            busId = "D1",
            routeName = "D1",
            destination = "UTown",
            currentLat = 1.2920,
            currentLng = 103.7800,
            etaMinutes = 12,
            stopsAway = 8,
            crowdLevel = "Medium",
            plateNumber = "SBS3422B",
            status = "coming",
            color = "#DB2777"
        ),
        BusInfo(
            busId = "A1",
            routeName = "A1",
            destination = "Kent Ridge MRT",
            currentLat = 1.2990,
            currentLng = 103.7720,
            etaMinutes = 15,
            stopsAway = 6,
            crowdLevel = "High",
            plateNumber = "SBS1234C",
            status = "delayed",
            color = "#DC2626"
        )
    ) }
    
    // === Original Mock Data ===
    
    val ROUTES by lazy { listOf(
        BusRoute(
            id = "D1",
            name = "D1",
            from = "Opp Hon Sui Sen",
            to = "UTown",
            color = "#DB2777",
            status = "Arriving",
            time = "2 min",
            crowd = "Low",
            number = "D1",
            nextArrival = 2,
            crowding = "Low",
            operational = true
        ),
        BusRoute(
            id = "D2",
            name = "D2",
            from = "Car Park 11",
            to = "UTown",
            color = "#7C3AED",
            status = "On Time",
            time = "5 min",
            crowd = "Med",
            number = "D2",
            nextArrival = 5,
            crowding = "Medium",
            operational = true
        ),
        BusRoute(
            id = "A1",
            name = "A1",
            from = "PGP Terminal",
            to = "Kent Ridge MRT",
            color = "#DC2626",
            status = "Delayed",
            time = "12 min",
            crowd = "High",
            number = "A1",
            nextArrival = 12,
            crowding = "High",
            operational = true
        ),
        BusRoute(
            id = "A2",
            name = "A2",
            from = "Kent Ridge MRT",
            to = "PGP Terminal",
            color = "#F59E0B",
            status = "Arriving",
            time = "3 min",
            crowd = "Low",
            number = "A2",
            nextArrival = 3,
            crowding = "Low",
            operational = true
        ),
        BusRoute(
            id = "BTC",
            name = "BTC",
            from = "Kent Ridge",
            to = "Bukit Timah",
            color = "#059669",
            status = "Scheduled",
            time = "25 min",
            crowd = "Low",
            number = "BTC",
            nextArrival = 25,
            crowding = "Medium",
            operational = false
        )
    ) }
    
    val COMMUNITIES by lazy { listOf(
        Community(
            name = "School of Computing",
            points = 15420,
            change = 245
        ),
        Community(
            name = "Faculty of Engineering",
            points = 14890,
            change = 180
        ),
        Community(
            name = "Faculty of Science",
            points = 14320,
            change = -45
        ),
        Community(
            name = "Faculty of Arts and Social Sciences",
            points = 13890,
            change = 120
        ),
        Community(
            name = "Business School",
            points = 13450,
            change = -80
        ),
        Community(
            name = "Faculty of Law",
            points = 12980,
            change = 95
        ),
        Community(
            name = "Faculty of Dentistry",
            points = 12540,
            change = -30
        ),
        Community(
            name = "Yong Loo Lin School of Medicine",
            points = 12100,
            change = 150
        ),
        Community(
            name = "School of Design and Environment",
            points = 11680,
            change = 60
        ),
        Community(
            name = "Yong Siew Toh Conservatory of Music",
            points = 11250,
            change = -15
        ),
        Community(
            name = "Faculty of Nursing",
            points = 10820,
            change = 110
        ),
        Community(
            name = "School of Public Health",
            points = 10400,
            change = 75
        ),
        Community(
            name = "School of Continuing and Lifelong Education",
            points = 9980,
            change = -25
        ),
        Community(
            name = "College of Humanities and Sciences",
            points = 9560,
            change = 40
        )
    ) }
    
    val SHOP_ITEMS by lazy { listOf(
        // === LiNUS Avatar Items ===
        
        // Face Items (眼镜/配饰) - 9 items (ISS glasses at front)
        ShopItem(
            id = "face_glasses_square",
            name = "Square Glasses 👓",
            type = "face",
            cost = 300,
            owned = false,
            equipped = false
        ),
        
        // Head Items (帽子/头饰) - 10 items
        ShopItem(
            id = "hat_grad",
            name = "Graduation Cap 🎓",
            type = "head",
            cost = 500,
            owned = true,
            equipped = false
        ),
        ShopItem(
            id = "hat_cap",
            name = "Orange Cap 🧢",
            type = "head",
            cost = 200,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_helmet",
            name = "Safety Helmet ⛑️",
            type = "head",
            cost = 300,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_beret",
            name = "Artist Beret 🎨",
            type = "head",
            cost = 300,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_crown",
            name = "Golden Crown 👑",
            type = "head",
            cost = 800,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_party",
            name = "Party Hat 🎉",
            type = "head",
            cost = 250,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_beanie",
            name = "Winter Beanie ❄️",
            type = "head",
            cost = 350,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_cowboy",
            name = "Cowboy Hat 🤠",
            type = "head",
            cost = 400,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_chef",
            name = "Chef Hat 👨‍🍳",
            type = "head",
            cost = 450,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "hat_wizard",
            name = "Wizard Hat 🧙",
            type = "head",
            cost = 700,
            owned = false,
            equipped = false
        ),
        
        // More Face Items
        ShopItem(
            id = "glasses_sun",
            name = "Cool Shades 😎",
            type = "face",
            cost = 300,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "face_goggles",
            name = "Safety Goggles 🥽",
            type = "face",
            cost = 250,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "glasses_nerd",
            name = "Nerd Glasses 🤓",
            type = "face",
            cost = 280,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "glasses_3d",
            name = "3D Glasses 🎬",
            type = "face",
            cost = 220,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "face_mask",
            name = "Superhero Mask 🦸",
            type = "face",
            cost = 450,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "face_monocle",
            name = "Fancy Monocle 🧐",
            type = "face",
            cost = 380,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "face_scarf",
            name = "Winter Scarf 🧣",
            type = "face",
            cost = 320,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "face_vr",
            name = "VR Headset 🥽",
            type = "face",
            cost = 600,
            owned = false,
            equipped = false
        ),
        
        // Body Items (服装) - 15 items (ISS white shirt at front)
        ShopItem(
            id = "body_white_shirt",
            name = "White Shirt 👔",
            type = "body",
            cost = 350,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "shirt_nus",
            name = "NUS Tee 🎓",
            type = "body",
            cost = 400,
            owned = true,
            equipped = true
        ),
        ShopItem(
            id = "shirt_hoodie",
            name = "Blue Hoodie 🧥",
            type = "body",
            cost = 600,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_plaid",
            name = "Engineering Plaid 👔",
            type = "body",
            cost = 400,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_suit",
            name = "Business Suit 🤵",
            type = "body",
            cost = 500,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_coat",
            name = "Lab Coat 🥼",
            type = "body",
            cost = 450,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_sports",
            name = "Sports Jersey ⚽",
            type = "body",
            cost = 550,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_kimono",
            name = "Traditional Kimono 👘",
            type = "body",
            cost = 650,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_tux",
            name = "Fancy Tuxedo 🎩",
            type = "body",
            cost = 800,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_superhero",
            name = "Superhero Cape 🦸",
            type = "body",
            cost = 700,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_doctor",
            name = "Doctor's Coat 👨‍⚕️",
            type = "body",
            cost = 480,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_pilot",
            name = "Pilot Uniform ✈️",
            type = "body",
            cost = 620,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_ninja",
            name = "Ninja Outfit 🥷",
            type = "body",
            cost = 750,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_scrubs",
            name = "Medical Scrubs 🏥",
            type = "body",
            cost = 450,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "body_polo",
            name = "Nurse Polo 👩‍⚕️",
            type = "body",
            cost = 400,
            owned = false,
            equipped = false
        ),
        
        // Badge Items (徽章/成就) - 10 items
        ShopItem(
            id = "badge_eco_warrior",
            name = "Eco Warrior Badge 🌿",
            type = "badge",
            cost = 1000,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_walker",
            name = "10K Steps Master 🚶",
            type = "badge",
            cost = 800,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_cyclist",
            name = "Cycling Champion 🚴",
            type = "badge",
            cost = 850,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_green",
            name = "Green Hero 🌱",
            type = "badge",
            cost = 600,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_pioneer",
            name = "EcoGo Pioneer 🏆",
            type = "badge",
            cost = 1200,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_streak",
            name = "30-Day Streak 🔥",
            type = "badge",
            cost = 900,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_social",
            name = "Social Butterfly 🦋",
            type = "badge",
            cost = 700,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_explorer",
            name = "Campus Explorer 🗺️",
            type = "badge",
            cost = 650,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_recycler",
            name = "Recycling Pro ♻️",
            type = "badge",
            cost = 550,
            owned = false,
            equipped = false
        ),
        ShopItem(
            id = "badge_legend",
            name = "EcoGo Legend ⭐",
            type = "badge",
            cost = 1500,
            owned = false,
            equipped = false
        )
    ) }
    
    val VOUCHERS by lazy { listOf(
        // Food & Beverage Vouchers
        Voucher(
            id = "v1",
            name = "Starbucks $5 Off",
            cost = 500,
            description = "Get $5 off your next purchase at Starbucks. Valid at all outlets.",
            available = true
        ),
        Voucher(
            id = "v2",
            name = "KFC $8 Discount",
            cost = 600,
            description = "$8 off on any order above $15. Dine-in or takeaway.",
            available = true
        ),
        Voucher(
            id = "v3",
            name = "Subway Free Cookie",
            cost = 300,
            description = "Free cookie with any 6-inch or footlong sub purchase.",
            available = true
        ),
        Voucher(
            id = "v4",
            name = "McDonald's McCombo",
            cost = 450,
            description = "Get a free McFlurry with any Extra Value Meal.",
            available = true
        ),
        Voucher(
            id = "v5",
            name = "Bubble Tea 50% Off",
            cost = 350,
            description = "50% discount on any bubble tea drink at Gong Cha or LiHO.",
            available = true
        ),
        Voucher(
            id = "v6",
            name = "Pizza Hut Buy 1 Get 1",
            cost = 900,
            description = "Buy 1 Large Pizza, Get 1 FREE (equal or lesser value).",
            available = true
        ),
        Voucher(
            id = "v7",
            name = "Campus Canteen $3 Off",
            cost = 250,
            description = "$3 off on meals at any NUS canteen. Valid weekdays only.",
            available = true
        ),
        Voucher(
            id = "v8",
            name = "Foodpanda $8 Off",
            cost = 650,
            description = "Save $8 on food delivery. Minimum order $20.",
            available = false
        ),
        
        // Transportation Vouchers
        Voucher(
            id = "v9",
            name = "Grab $10 Voucher",
            cost = 800,
            description = "$10 credit for Grab rides. Valid for 30 days.",
            available = true
        ),
        Voucher(
            id = "v10",
            name = "Grab $5 Voucher",
            cost = 400,
            description = "$5 credit for Grab rides or GrabFood orders.",
            available = true
        ),
        Voucher(
            id = "v11",
            name = "TransitLink $10 Top-up",
            cost = 850,
            description = "$10 top-up credit for your EZ-Link or NETS FlashPay card.",
            available = true
        ),
        Voucher(
            id = "v12",
            name = "Gojek $8 Voucher",
            cost = 650,
            description = "$8 off your next GoRide or GoCar trip.",
            available = true
        ),
        
        // Retail & Shopping Vouchers
        Voucher(
            id = "v13",
            name = "NUS Bookstore 15% Off",
            cost = 450,
            description = "15% discount on books and stationery. Excludes textbooks.",
            available = true
        ),
        Voucher(
            id = "v14",
            name = "FairPrice $5 Voucher",
            cost = 400,
            description = "$5 off at any FairPrice supermarket. Minimum spend $30.",
            available = true
        ),
        Voucher(
            id = "v15",
            name = "Uniqlo $10 Off",
            cost = 750,
            description = "$10 discount on purchases above $50.",
            available = true
        ),
        Voucher(
            id = "v16",
            name = "Popular Bookstore $8 Off",
            cost = 550,
            description = "$8 off stationery, books, or gadgets at Popular.",
            available = false
        ),
        Voucher(
            id = "v17",
            name = "Decathlon 20% Off",
            cost = 800,
            description = "20% off on sports equipment and activewear.",
            available = true
        ),
        
        // Entertainment & Lifestyle
        Voucher(
            id = "v18",
            name = "GV Cinema Ticket",
            cost = 950,
            description = "1 complimentary movie ticket at Golden Village cinemas.",
            available = true
        ),
        Voucher(
            id = "v19",
            name = "Shaw Theatre $5 Off",
            cost = 400,
            description = "$5 off any movie ticket at Shaw Theatres.",
            available = true
        ),
        Voucher(
            id = "v20",
            name = "ActiveSG $10 Credit",
            cost = 600,
            description = "$10 credit for ActiveSG sports facilities and classes.",
            available = true
        ),
        Voucher(
            id = "v21",
            name = "Kinokuniya $15 Off",
            cost = 700,
            description = "$15 off books purchase above $50.",
            available = false
        ),
        
        // Health & Wellness
        Voucher(
            id = "v22",
            name = "Guardian $5 Voucher",
            cost = 400,
            description = "$5 off health and beauty products. Min spend $25.",
            available = true
        ),
        Voucher(
            id = "v23",
            name = "Watson's $8 Off",
            cost = 550,
            description = "$8 discount on personal care items.",
            available = true
        ),
        Voucher(
            id = "v24",
            name = "Spa 30% Discount",
            cost = 1200,
            description = "30% off any spa treatment at Yunomori Onsen.",
            available = false
        ),
        
        // Special Limited Edition
        Voucher(
            id = "v25",
            name = "🎁 Mystery Voucher Box",
            cost = 1500,
            description = "Random voucher worth $20-$50. Limited quantity!",
            available = true
        ),
        Voucher(
            id = "v26",
            name = "🌟 Premium Combo Pack",
            cost = 2000,
            description = "5x $5 vouchers (Food, Transport, Retail). Best value!",
            available = true
        ),
        Voucher(
            id = "v27",
            name = "🔥 Flash Sale - Grab $15",
            cost = 999,
            description = "$15 Grab credit. Limited time offer! Hurry!",
            available = true
        )
    ) }
    
    val WALKING_ROUTES by lazy { listOf(
        WalkingRoute(
            id = 1,
            title = "Central Library Loop",
            time = "15 mins",
            distance = "1.2 km",
            calories = "80 cal",
            tags = listOf("Scenic", "Easy"),
            description = "A pleasant walk around the central library area"
        ),
        WalkingRoute(
            id = 2,
            title = "Kent Ridge Trail",
            time = "25 mins",
            distance = "2.0 km",
            calories = "150 cal",
            tags = listOf("Nature", "Moderate"),
            description = "Experience nature on this moderate trail"
        ),
        WalkingRoute(
            id = 3,
            title = "UTown Circuit",
            time = "12 mins",
            distance = "0.9 km",
            calories = "60 cal",
            tags = listOf("Urban", "Easy"),
            description = "Quick walk around UTown"
        )
    ) }
    
    val ACTIVITIES by lazy { listOf(
        Activity(
            id = "activity1",
            title = "Campus Clean-Up Day",
            description = "Join us for campus beautification at Central Library",
            type = "OFFLINE",
            status = "PUBLISHED",
            rewardCredits = 150,
            maxParticipants = 50,
            currentParticipants = 23,
            startTime = "2026-02-05T10:00:00",
            endTime = "2026-02-05T14:00:00"
        ),
        Activity(
            id = "activity2",
            title = "Eco Workshop",
            description = "Learn about sustainability practices at UTown",
            type = "OFFLINE",
            status = "PUBLISHED",
            rewardCredits = 200,
            maxParticipants = 30,
            currentParticipants = 18,
            startTime = "2026-02-12T14:00:00",
            endTime = "2026-02-12T17:00:00"
        ),
        Activity(
            id = "activity3",
            title = "Green Run 5K",
            description = "Charity run for the environment at Kent Ridge",
            type = "OFFLINE",
            status = "ONGOING",
            rewardCredits = 300,
            maxParticipants = 100,
            currentParticipants = 67,
            startTime = "2026-02-20T07:00:00",
            endTime = "2026-02-20T10:00:00"
        ),
        Activity(
            id = "activity4",
            title = "Recycling Drive",
            description = "Drop off your recyclables at PGP",
            type = "OFFLINE",
            status = "PUBLISHED",
            rewardCredits = 100,
            maxParticipants = null,
            currentParticipants = 45,
            startTime = "2026-02-28T09:00:00",
            endTime = "2026-02-28T18:00:00"
        )
    ) }
    
    val ACHIEVEMENTS by lazy { listOf(
        // ── 新人入门 (Newcomer) ──────────────────
        Achievement(
            id = "a1",
            name = "First Ride",
            description = "Take your first eco-friendly trip",
            unlocked = true,
            howToUnlock = "Complete your very first eco-friendly trip using public transport, cycling, or walking. Any single trip counts!"
        ),
        Achievement(
            id = "a2",
            name = "First Check-in",
            description = "Complete your first daily check-in",
            unlocked = true,
            howToUnlock = "Open the app and tap the daily check-in button for the very first time. Welcome aboard!"
        ),
        Achievement(
            id = "a3",
            name = "First Activity",
            description = "Join your first community event",
            unlocked = true,
            howToUnlock = "Sign up and participate in any community eco-activity or campus green event through the app."
        ),
        Achievement(
            id = "a4",
            name = "Profile Complete",
            description = "Fill in all profile details",
            unlocked = true,
            howToUnlock = "Complete your profile by setting a nickname, choosing a faculty, and customizing your mascot outfit."
        ),
        // ── 连续打卡 (Streak) ────────────────────
        Achievement(
            id = "a5",
            name = "Week Warrior",
            description = "7-day eco transport streak",
            unlocked = true,
            howToUnlock = "Log an eco-friendly trip every day for 7 consecutive days. Missing a day resets the streak."
        ),
        Achievement(
            id = "a6",
            name = "Habit Builder",
            description = "14-day consecutive check-in",
            unlocked = false,
            howToUnlock = "Check in to the app every day for 14 consecutive days. Build a habit and keep the streak going!"
        ),
        Achievement(
            id = "a7",
            name = "Month Master",
            description = "30-day check-in streak",
            unlocked = false,
            howToUnlock = "Maintain a daily check-in streak for a full 30 days. Dedication pays off!"
        ),
        Achievement(
            id = "a8",
            name = "Iron Will",
            description = "60-day eco trip streak",
            unlocked = false,
            howToUnlock = "Log at least one eco-friendly trip every single day for 60 consecutive days. True commitment!"
        ),
        // ── 积分里程碑 (Points Milestone) ─────────
        Achievement(
            id = "a9",
            name = "Century Club",
            description = "Earn 100 points",
            unlocked = true,
            howToUnlock = "Accumulate a total of 100 EcoGo points from trips, check-ins, and community activities."
        ),
        Achievement(
            id = "a10",
            name = "Points Pro",
            description = "Earn 500 points",
            unlocked = false,
            howToUnlock = "Accumulate a total of 500 EcoGo points. Keep riding, walking, and checking in!"
        ),
        Achievement(
            id = "a11",
            name = "Points Legend",
            description = "Earn 2000 points",
            unlocked = false,
            howToUnlock = "Reach a lifetime total of 2000 EcoGo points. You are a true eco legend!"
        ),
        // ── 出行方式 (Transport) ─────────────────
        Achievement(
            id = "a12",
            name = "Cycling Champion",
            description = "Complete 10 cycling trips",
            unlocked = false,
            howToUnlock = "Log 10 trips using a bicycle. Campus cycling, bike-share, or personal bikes all count."
        ),
        Achievement(
            id = "a13",
            name = "Walk the Talk",
            description = "Walk 50,000 steps total",
            unlocked = false,
            howToUnlock = "Accumulate 50,000 walking steps tracked through the app. Every step counts towards a greener campus!"
        ),
        Achievement(
            id = "a14",
            name = "Bus Regular",
            description = "Take 20 bus trips",
            unlocked = false,
            howToUnlock = "Log 20 trips using public buses. Campus shuttles and city buses both count."
        ),
        Achievement(
            id = "a15",
            name = "Carbon Cutter",
            description = "Save 10kg CO₂ emissions",
            unlocked = false,
            howToUnlock = "By choosing eco-friendly transport, reduce your carbon footprint by a total of 10kg CO₂ compared to driving."
        ),
        // ── 社交社区 (Social) ────────────────────
        Achievement(
            id = "a16",
            name = "Social Butterfly",
            description = "Join 5 community activities",
            unlocked = false,
            howToUnlock = "Participate in 5 different community eco-activities or events organized through the EcoGo app."
        ),
        Achievement(
            id = "a17",
            name = "Team Player",
            description = "Invite 3 friends to EcoGo",
            unlocked = false,
            howToUnlock = "Share your invite link and get 3 friends to sign up and complete their first trip on EcoGo."
        ),
        Achievement(
            id = "a18",
            name = "Community Leader",
            description = "Organize a green event",
            unlocked = false,
            howToUnlock = "Create and host a community eco-event (e.g. campus cleanup, tree planting) through the app with at least 5 participants."
        ),
        // ── 特殊成就 (Special) ──────────────────
        Achievement(
            id = "a19",
            name = "Master Saver",
            description = "Redeem 10 vouchers",
            unlocked = false,
            howToUnlock = "Use your points to redeem 10 vouchers from the Rewards store. Each unique redemption counts."
        ),
        Achievement(
            id = "a20",
            name = "Eco Champion",
            description = "Reach #1 on leaderboard",
            unlocked = false,
            howToUnlock = "Climb to the #1 position on your faculty or community leaderboard during any weekly or monthly cycle."
        )
    ) }
    
    val HISTORY by lazy { listOf(
        HistoryItem(
            id = 1,
            action = "Bus Ride (D1)",
            time = "2 hours ago",
            points = "+25",
            type = "earn"
        ),
        HistoryItem(
            id = 2,
            action = "Redeemed Voucher",
            time = "1 day ago",
            points = "-500",
            type = "spend"
        ),
        HistoryItem(
            id = 3,
            action = "Walked to Class",
            time = "2 days ago",
            points = "+15",
            type = "earn"
        ),
        HistoryItem(
            id = 4,
            action = "Joined Activity",
            time = "3 days ago",
            points = "+150",
            type = "earn"
        )
    ) }
    
    val FACULTIES by lazy { listOf(
        Faculty(
            id = "soc",
            name = "School of Computing",
            x = 0.3f,
            y = 0.4f,
            score = 15420,
            rank = 1
        ),
        Faculty(
            id = "eng",
            name = "Faculty of Engineering",
            x = 0.5f,
            y = 0.3f,
            score = 14890,
            rank = 2
        ),
        Faculty(
            id = "sci",
            name = "Faculty of Science",
            x = 0.7f,
            y = 0.5f,
            score = 14320,
            rank = 3
        ),
        Faculty(
            id = "fass",
            name = "Faculty of Arts and Social Sciences",
            x = 0.4f,
            y = 0.7f,
            score = 13890,
            rank = 4
        ),
        Faculty(
            id = "biz",
            name = "Business School",
            x = 0.6f,
            y = 0.6f,
            score = 13450,
            rank = 5
        ),
        Faculty(
            id = "law",
            name = "Faculty of Law",
            x = 0.2f,
            y = 0.6f,
            score = 12980,
            rank = 6
        ),
        Faculty(
            id = "dent",
            name = "Faculty of Dentistry",
            x = 0.8f,
            y = 0.3f,
            score = 12540,
            rank = 7
        ),
        Faculty(
            id = "med",
            name = "Yong Loo Lin School of Medicine",
            x = 0.3f,
            y = 0.2f,
            score = 12100,
            rank = 8
        ),
        Faculty(
            id = "sde",
            name = "School of Design and Environment",
            x = 0.7f,
            y = 0.7f,
            score = 11680,
            rank = 9
        ),
        Faculty(
            id = "music",
            name = "Yong Siew Toh Conservatory of Music",
            x = 0.5f,
            y = 0.8f,
            score = 11250,
            rank = 10
        ),
        Faculty(
            id = "nursing",
            name = "Faculty of Nursing",
            x = 0.2f,
            y = 0.3f,
            score = 10820,
            rank = 11
        ),
        Faculty(
            id = "sph",
            name = "School of Public Health",
            x = 0.8f,
            y = 0.6f,
            score = 10400,
            rank = 12
        ),
        Faculty(
            id = "cle",
            name = "School of Continuing and Lifelong Education",
            x = 0.6f,
            y = 0.2f,
            score = 9980,
            rank = 13
        ),
        Faculty(
            id = "chs",
            name = "College of Humanities and Sciences",
            x = 0.4f,
            y = 0.5f,
            score = 9560,
            rank = 14
        )
    ) }
    
    // Faculty data for signup with outfit configurations (14 faculties)
    val FACULTY_DATA by lazy { listOf(
        FacultyData(
            id = "iss",
            name = "Institute of Systems Science",
            color = "#6B7280",
            slogan = "Systems for Tomorrow 🔬",
            outfit = Outfit(head = "none", face = "face_glasses_square", body = "body_white_shirt")
        ),
        FacultyData(
            id = "soc",
            name = "School of Computing",
            color = "#3B82F6",
            slogan = "Code the Future 💻",
            outfit = Outfit(head = "hat_cap", face = "none", body = "shirt_hoodie")
        ),
        FacultyData(
            id = "eng",
            name = "Faculty of Engineering",
            color = "#EF4444",
            slogan = "Building the Future 🛠️",
            outfit = Outfit(head = "hat_helmet", face = "none", body = "body_plaid")
        ),
        FacultyData(
            id = "sci",
            name = "Faculty of Science",
            color = "#6366F1",
            slogan = "Discovering Truth 🧪",
            outfit = Outfit(head = "none", face = "face_goggles", body = "body_coat")
        ),
        FacultyData(
            id = "fass",
            name = "Faculty of Arts and Social Sciences",
            color = "#F97316",
            slogan = "Create & Inspire 🎨",
            outfit = Outfit(head = "hat_beret", face = "none", body = "body_kimono")
        ),
        FacultyData(
            id = "biz",
            name = "Business School",
            color = "#EAB308",
            slogan = "Leading the Way 💼",
            outfit = Outfit(head = "none", face = "face_monocle", body = "body_suit")
        ),
        FacultyData(
            id = "law",
            name = "Faculty of Law",
            color = "#7C3AED",
            slogan = "Justice for All ⚖️",
            outfit = Outfit(head = "hat_grad", face = "none", body = "body_tux")
        ),
        FacultyData(
            id = "dent",
            name = "Faculty of Dentistry",
            color = "#14B8A6",
            slogan = "Smile with Confidence 🦷",
            outfit = Outfit(head = "none", face = "face_mask", body = "body_doctor")
        ),
        FacultyData(
            id = "med",
            name = "Yong Loo Lin School of Medicine",
            color = "#10B981",
            slogan = "Saving Lives 🩺",
            outfit = Outfit(head = "none", face = "none", body = "body_scrubs")
        ),
        FacultyData(
            id = "sde",
            name = "School of Design and Environment",
            color = "#06B6D4",
            slogan = "Design a Better World 🌍",
            outfit = Outfit(head = "hat_beret", face = "none", body = "body_superhero")
        ),
        FacultyData(
            id = "music",
            name = "Yong Siew Toh Conservatory of Music",
            color = "#EC4899",
            slogan = "Harmony in Motion 🎵",
            outfit = Outfit(head = "hat_party", face = "none", body = "shirt_nus")
        ),
        FacultyData(
            id = "nursing",
            name = "Faculty of Nursing",
            color = "#F43F5E",
            slogan = "Care with Heart 💉",
            outfit = Outfit(head = "none", face = "face_scarf", body = "body_polo")
        ),
        FacultyData(
            id = "sph",
            name = "School of Public Health",
            color = "#84CC16",
            slogan = "Health for All 🏥",
            outfit = Outfit(head = "hat_cap", face = "none", body = "body_sports")
        ),
        FacultyData(
            id = "cle",
            name = "School of Continuing and Lifelong Education",
            color = "#A855F7",
            slogan = "Never Stop Learning 📚",
            outfit = Outfit(head = "hat_cowboy", face = "none", body = "body_pilot")
        ),
        FacultyData(
            id = "chs",
            name = "College of Humanities and Sciences",
            color = "#0EA5E9",
            slogan = "Explore the Unknown 🔮",
            outfit = Outfit(head = "hat_wizard", face = "none", body = "body_ninja")
        )
    ) }
    
    // Check-in Mock Data
    val CHECK_IN_STATUS by lazy { CheckInStatus(
        userId = "user123",
        lastCheckInDate = "2026-01-30",
        consecutiveDays = 5,
        totalCheckIns = 28,
        pointsEarned = 350
    ) }
    
    // Check-in History Mock Data (last 30 days of check-ins)
    val CHECK_IN_HISTORY by lazy { listOf(
        CheckIn(
            id = "checkin_001",
            userId = "user123",
            checkInDate = "2026-02-02",
            pointsEarned = 10,
            consecutiveDays = 8,
            timestamp = "2026-02-02T08:30:00Z"
        ),
        CheckIn(
            id = "checkin_002",
            userId = "user123",
            checkInDate = "2026-02-01",
            pointsEarned = 10,
            consecutiveDays = 7,
            timestamp = "2026-02-01T09:15:00Z"
        ),
        CheckIn(
            id = "checkin_003",
            userId = "user123",
            checkInDate = "2026-01-31",
            pointsEarned = 10,
            consecutiveDays = 6,
            timestamp = "2026-01-31T07:45:00Z"
        ),
        CheckIn(
            id = "checkin_004",
            userId = "user123",
            checkInDate = "2026-01-30",
            pointsEarned = 10,
            consecutiveDays = 5,
            timestamp = "2026-01-30T08:20:00Z"
        ),
        CheckIn(
            id = "checkin_005",
            userId = "user123",
            checkInDate = "2026-01-29",
            pointsEarned = 10,
            consecutiveDays = 4,
            timestamp = "2026-01-29T09:00:00Z"
        ),
        CheckIn(
            id = "checkin_006",
            userId = "user123",
            checkInDate = "2026-01-28",
            pointsEarned = 10,
            consecutiveDays = 3,
            timestamp = "2026-01-28T08:10:00Z"
        ),
        CheckIn(
            id = "checkin_007",
            userId = "user123",
            checkInDate = "2026-01-27",
            pointsEarned = 10,
            consecutiveDays = 2,
            timestamp = "2026-01-27T07:55:00Z"
        ),
        CheckIn(
            id = "checkin_008",
            userId = "user123",
            checkInDate = "2026-01-26",
            pointsEarned = 10,
            consecutiveDays = 1,
            timestamp = "2026-01-26T08:30:00Z"
        ),
        CheckIn(
            id = "checkin_009",
            userId = "user123",
            checkInDate = "2026-01-24",
            pointsEarned = 10,
            consecutiveDays = 1,
            timestamp = "2026-01-24T09:20:00Z"
        ),
        CheckIn(
            id = "checkin_010",
            userId = "user123",
            checkInDate = "2026-01-23",
            pointsEarned = 10,
            consecutiveDays = 2,
            timestamp = "2026-01-23T08:40:00Z"
        ),
        CheckIn(
            id = "checkin_011",
            userId = "user123",
            checkInDate = "2026-01-22",
            pointsEarned = 10,
            consecutiveDays = 1,
            timestamp = "2026-01-22T07:50:00Z"
        ),
        CheckIn(
            id = "checkin_012",
            userId = "user123",
            checkInDate = "2026-01-20",
            pointsEarned = 10,
            consecutiveDays = 1,
            timestamp = "2026-01-20T08:15:00Z"
        ),
        CheckIn(
            id = "checkin_013",
            userId = "user123",
            checkInDate = "2026-01-19",
            pointsEarned = 10,
            consecutiveDays = 3,
            timestamp = "2026-01-19T09:10:00Z"
        ),
        CheckIn(
            id = "checkin_014",
            userId = "user123",
            checkInDate = "2026-01-18",
            pointsEarned = 10,
            consecutiveDays = 2,
            timestamp = "2026-01-18T08:05:00Z"
        ),
        CheckIn(
            id = "checkin_015",
            userId = "user123",
            checkInDate = "2026-01-17",
            pointsEarned = 10,
            consecutiveDays = 1,
            timestamp = "2026-01-17T07:45:00Z"
        )
    ) }
    
    // Daily Goal Mock Data
    val DAILY_GOAL by lazy { DailyGoal(
        id = "goal123",
        userId = "user123",
        date = "2026-01-31",
        stepGoal = 10000,
        currentSteps = 6500,
        tripGoal = 3,
        currentTrips = 2,
        co2SavedGoal = 2.0f,
        currentCo2Saved = 1.5f
    ) }
    
    // Weather Mock Data
    val WEATHER by lazy { Weather(
        location = "NUS",
        temperature = 21.0,
        description = "Sunny",
        icon = "01d",
        humidity = "75",
        airQuality = 45,
        aqiLevel = "Good",
        recommendation = "Perfect day for walking or cycling!"
    ) }
    
    // Notifications Mock Data
    val NOTIFICATIONS by lazy { listOf(
        Notification(
            id = "notif1",
            type = "activity",
            title = "Upcoming Activity",
            message = "Campus Clean-Up starts in 1 hour!",
            timestamp = "2026-01-31T09:00:00",
            isRead = false,
            actionUrl = "activities/activity1"
        ),
        Notification(
            id = "notif2",
            type = "bus_delay",
            title = "Bus Delay",
            message = "D1 is delayed by 5 minutes",
            timestamp = "2026-01-31T08:45:00",
            isRead = false,
            actionUrl = "routes"
        ),
        Notification(
            id = "notif3",
            type = "achievement",
            title = "New Achievement!",
            message = "You've unlocked 'Week Warrior' badge",
            timestamp = "2026-01-31T08:30:00",
            isRead = true,
            actionUrl = "profile/achievements"
        )
    ) }
    
    // Carbon Footprint Mock Data
    val CARBON_FOOTPRINT by lazy { CarbonFootprint(
        userId = "user123",
        period = "monthly",
        co2Saved = 12.5f,
        equivalentTrees = 3,
        tripsByBus = 45,
        tripsByWalking = 28,
        tripsByBicycle = 5
    ) }
    
    // Friends Mock Data
    val FRIENDS = listOf(
        Friend(
            userId = "friend1",
            nickname = "Alex Chen",
            avatarUrl = null,
            points = 920,
            rank = 1,
            faculty = "School of Computing"
        ),
        Friend(
            userId = "friend2",
            nickname = "Sarah Tan",
            avatarUrl = null,
            points = 875,
            rank = 2,
            faculty = "Faculty of Engineering"
        ),
        Friend(
            userId = "friend3",
            nickname = "Kevin Wong",
            avatarUrl = null,
            points = 850,
            rank = 3,
            faculty = "School of Computing"
        )
    )
    
    // Friend Activities Mock Data
    val FRIEND_ACTIVITIES = listOf(
        FriendActivity(
            friendId = "friend1",
            friendName = "Alex Chen",
            action = "joined_activity",
            timestamp = "30 mins ago",
            details = "Joined Campus Clean-Up Day"
        ),
        FriendActivity(
            friendId = "friend2",
            friendName = "Sarah Tan",
            action = "earned_badge",
            timestamp = "1 hour ago",
            details = "Unlocked 'Century Club' achievement"
        )
    )
    
    // Shop Products Mock Data
    val PRODUCTS = listOf(
        // Voucher Type
        Product(
            id = "p1",
            name = "Starbucks $5 Off",
            type = "voucher",
            category = "food",
            description = "Valid at all campus locations",
            pointsPrice = 500,
            cashPrice = 3.00,
            available = true,
            imageUrl = null,
            tags = listOf("popular", "food")
        ),
        Product(
            id = "p2",
            name = "Grab $10 Voucher",
            type = "voucher",
            category = "transport",
            description = "$10 credit for Grab rides",
            pointsPrice = 800,
            cashPrice = 6.00,
            available = true,
            tags = listOf("transport")
        ),
        Product(
            id = "p3",
            name = "Foodpanda $8 Off",
            type = "voucher",
            category = "food",
            description = "Save $8 on food delivery",
            pointsPrice = 650,
            cashPrice = 5.00,
            available = false,  // Temporarily unavailable
            tags = listOf("food")
        ),
        
        // Goods Type
        Product(
            id = "p4",
            name = "Eco Bamboo Bottle",
            type = "goods",
            category = "eco_product",
            description = "Sustainable 500ml water bottle",
            pointsPrice = 1200,
            cashPrice = 15.00,
            available = true,
            stock = 50,
            brand = "EcoWare",
            tags = listOf("eco", "popular")
        ),
        Product(
            id = "p5",
            name = "EcoGo T-Shirt",
            type = "goods",
            category = "merchandise",
            description = "Official EcoGo merchandise",
            pointsPrice = null,  // 只支持现金
            cashPrice = 20.00,
            available = true,
            stock = 30,
            brand = "EcoGo",
            tags = listOf("merchandise")
        ),
        Product(
            id = "p6",
            name = "Tree Planting Certificate",
            type = "goods",
            category = "digital",
            description = "Digital certificate - Plant a tree in your name",
            pointsPrice = 300,
            cashPrice = null,  // 只支持积分
            available = true,
            stock = null,  // 无限库存
            tags = listOf("eco", "digital")
        ),
        Product(
            id = "p7",
            name = "Reusable Tote Bag",
            type = "goods",
            category = "eco_product",
            description = "Canvas tote bag for sustainable shopping",
            pointsPrice = 600,
            cashPrice = 8.00,
            available = true,
            stock = 100,
            brand = "EcoWare",
            tags = listOf("eco")
        ),
        Product(
            id = "p8",
            name = "NUS Bookstore 15% Off",
            type = "voucher",
            category = "merchandise",
            description = "15% discount on books and stationery",
            pointsPrice = 450,
            cashPrice = 3.50,
            available = true,
            validUntil = "2026-12-31",
            tags = listOf("education")
        )
    )
    
    // Green Spots Mock Data
    val GREEN_SPOTS = listOf(
        GreenSpot(
            id = "spot1",
            name = "Campus Heritage Tree",
            lat = 1.2966,
            lng = 103.7764,
            type = "TREE",
            reward = 50,
            description = "A century-old tree on campus, witnessing the growth of countless students.",
            collected = false
        ),
        GreenSpot(
            id = "spot2",
            name = "Central Recycling Station",
            lat = 1.2970,
            lng = 103.7770,
            type = "RECYCLE_BIN",
            reward = 30,
            description = "Campus center recycling station, supporting multiple material types.",
            collected = false
        ),
        GreenSpot(
            id = "spot3",
            name = "Eco Park",
            lat = 1.2960,
            lng = 103.7758,
            type = "PARK",
            reward = 100,
            description = "Campus ecological park with rich vegetation and wildlife.",
            collected = true
        ),
        GreenSpot(
            id = "spot4",
            name = "Sustainability Center",
            lat = 1.2975,
            lng = 103.7780,
            type = "LANDMARK",
            reward = 75,
            description = "Campus sustainability research and education center.",
            collected = false
        )
    )
    
    // Challenges Mock Data
    val CHALLENGES = listOf(
        Challenge(
            id = "ch1",
            title = "Weekly Green Transport Challenge",
            description = "Complete 10 green trips this week and top the eco leaderboard!",
            type = "INDIVIDUAL",
            target = 10,
            current = 6,
            reward = 500,
            badge = "a6",
            startTime = "2026-02-03T00:00:00",
            endTime = "2026-02-09T23:59:59",
            participants = 342,
            topUsers = listOf(
                User(id = "u1", username = "Alex Chen", points = 9),
                User(id = "u2", username = "Sarah Tan", points = 8),
                User(id = "u3", username = "Kevin Wong", points = 7)
            ),
            status = "ACTIVE",
            icon = "🚶"
        ),
        Challenge(
            id = "ch2",
            title = "Faculty Championship",
            description = "Represent your faculty and compete for the champion trophy!",
            type = "FACULTY",
            target = 5000,
            current = 3250,
            reward = 1000,
            badge = null,
            startTime = "2026-02-01T00:00:00",
            endTime = "2026-02-29T23:59:59",
            participants = 1520,
            topUsers = listOf(
                User(id = "soc", username = "School of Computing", points = 3250),
                User(id = "eng", username = "Engineering", points = 3100),
                User(id = "sci", username = "Science", points = 2890)
            ),
            status = "ACTIVE",
            icon = "🏆"
        ),
        Challenge(
            id = "ch3",
            title = "Carbon Footprint Master",
            description = "Reduce 500g CO₂ emissions this month",
            type = "INDIVIDUAL",
            target = 500,
            current = 325,
            reward = 300,
            badge = "a5",
            startTime = "2026-02-01T00:00:00",
            endTime = "2026-02-28T23:59:59",
            participants = 567,
            topUsers = emptyList(),
            status = "ACTIVE",
            icon = "🌱"
        ),
        Challenge(
            id = "ch4",
            title = "Weekend Hiking Group",
            description = "Team up to complete a 5km hiking trail",
            type = "TEAM",
            target = 5,
            current = 5,
            reward = 250,
            badge = "a4",
            startTime = "2026-01-25T00:00:00",
            endTime = "2026-01-31T23:59:59",
            participants = 45,
            topUsers = emptyList(),
            status = "COMPLETED",
            icon = "🥾"
        )
    )
    
    // Feed Items Mock Data
    val FEED_ITEMS = listOf(
        FeedItem(
            id = "f1",
            userId = "friend1",
            username = "Alex Chen",
            type = "TRIP",
            content = "Completed a green trip, saved 125g CO₂!",
            timestamp = System.currentTimeMillis() - 1800000, // 30 mins ago
            likes = 12
        ),
        FeedItem(
            id = "f2",
            userId = "friend2",
            username = "Sarah Tan",
            type = "ACHIEVEMENT",
            content = "Unlocked 'Week Warrior' achievement!",
            timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
            likes = 25
        ),
        FeedItem(
            id = "f3",
            userId = "friend3",
            username = "Kevin Wong",
            type = "ACTIVITY",
            content = "Joined Campus Clean-Up Day activity",
            timestamp = System.currentTimeMillis() - 7200000, // 2 hours ago
            likes = 8
        ),
        FeedItem(
            id = "f4",
            userId = "friend4",
            username = "Emily Liu",
            type = "CHALLENGE",
            content = "Ranked #1 in 'Weekly Green Transport Challenge'!",
            timestamp = System.currentTimeMillis() - 10800000, // 3 hours ago
            likes = 35
        ),
        FeedItem(
            id = "f5",
            userId = "friend5",
            username = "David Ng",
            type = "TRIP",
            content = "Cycled 3.5 km, earned 175 points",
            timestamp = System.currentTimeMillis() - 18000000, // 5 hours ago
            likes = 6
        ),
        FeedItem(
            id = "f6",
            userId = "friend1",
            username = "Alex Chen",
            type = "ACHIEVEMENT",
            content = "Reduced 5kg CO₂ emissions in total!",
            timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
            likes = 42
        ),
        FeedItem(
            id = "f7",
            userId = "friend6",
            username = "Jennifer Lim",
            type = "TRIP",
            content = "Walked to library, completed daily step goal!",
            timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
            likes = 15
        ),
        FeedItem(
            id = "f8",
            userId = "friend7",
            username = "Michael Tan",
            type = "CHALLENGE",
            content = "Accepted 'Carbon Footprint Master' challenge",
            timestamp = System.currentTimeMillis() - 259200000, // 3 days ago
            likes = 9
        )
    )
    
    // Home Banners (Phase 1 - Simple)
    val HOME_BANNERS = listOf(
        HomeBanner(
            id = "banner_1",
            title = "New Challenge Available!",
            subtitle = "Complete 10K steps today",
            backgroundColor = "#15803D",
            actionText = "View",
            actionTarget = "challenges"
        ),
        HomeBanner(
            id = "banner_2",
            title = "Limited Time Offer",
            subtitle = "50% off selected vouchers",
            backgroundColor = "#F97316",
            actionText = "Shop Now",
            actionTarget = "vouchers"
        ),
        HomeBanner(
            id = "banner_3",
            title = "Welcome to EcoGo!",
            subtitle = "Start your eco-friendly journey today",
            backgroundColor = "#3B82F6",
            actionText = null,
            actionTarget = null
        )
    )
}
