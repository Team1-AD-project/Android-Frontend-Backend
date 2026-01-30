package com.ecogo.data

object MockData {
    
    val ROUTES = listOf(
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
    )
    
    val COMMUNITIES = listOf(
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
            name = "Faculty of Arts & Social Sciences",
            points = 13890,
            change = 120
        ),
        Community(
            name = "School of Business",
            points = 13450,
            change = -80
        )
    )
    
    val SHOP_ITEMS = listOf(
        ShopItem(
            id = "hat1",
            name = "Baseball Cap",
            type = "head",
            cost = 150,
            owned = false
        ),
        ShopItem(
            id = "glasses1",
            name = "Cool Sunglasses",
            type = "face",
            cost = 100,
            owned = false
        ),
        ShopItem(
            id = "shirt1",
            name = "NUS T-Shirt",
            type = "body",
            cost = 200,
            owned = true
        ),
        ShopItem(
            id = "hat2",
            name = "Wizard Hat",
            type = "head",
            cost = 300,
            owned = false
        ),
        ShopItem(
            id = "glasses2",
            name = "Reading Glasses",
            type = "face",
            cost = 120,
            owned = false
        )
    )
    
    val VOUCHERS = listOf(
        Voucher(
            id = "v1",
            name = "Starbucks $5 Off",
            cost = 500,
            description = "Get $5 off your next purchase at Starbucks",
            available = true
        ),
        Voucher(
            id = "v2",
            name = "Grab $10 Voucher",
            cost = 800,
            description = "$10 credit for Grab rides",
            available = true
        ),
        Voucher(
            id = "v3",
            name = "Foodpanda $8 Off",
            cost = 650,
            description = "Save $8 on food delivery",
            available = false
        ),
        Voucher(
            id = "v4",
            name = "NUS Bookstore 15% Off",
            cost = 450,
            description = "15% discount on books and stationery",
            available = true
        )
    )
    
    val WALKING_ROUTES = listOf(
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
    )
    
    val ACTIVITIES = listOf(
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
    )
    
    val ACHIEVEMENTS = listOf(
        Achievement(
            id = "a1",
            name = "First Ride",
            description = "Take your first eco-friendly trip",
            unlocked = true
        ),
        Achievement(
            id = "a2",
            name = "Week Warrior",
            description = "Use eco transport for 7 days straight",
            unlocked = true
        ),
        Achievement(
            id = "a3",
            name = "Century Club",
            description = "Earn 100 points",
            unlocked = true
        ),
        Achievement(
            id = "a4",
            name = "Social Butterfly",
            description = "Join 5 community activities",
            unlocked = false
        ),
        Achievement(
            id = "a5",
            name = "Master Saver",
            description = "Redeem 10 vouchers",
            unlocked = false
        ),
        Achievement(
            id = "a6",
            name = "Eco Champion",
            description = "Reach top of leaderboard",
            unlocked = false
        )
    )
    
    val HISTORY = listOf(
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
    )
    
    val FACULTIES = listOf(
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
            name = "FASS",
            x = 0.4f,
            y = 0.7f,
            score = 13890,
            rank = 4
        ),
        Faculty(
            id = "biz",
            name = "School of Business",
            x = 0.6f,
            y = 0.6f,
            score = 13450,
            rank = 5
        )
    )
}
