package com.example.EcoGo.config;

import com.example.EcoGo.model.Voucher;
import com.example.EcoGo.repository.VoucherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化配置
 * 在应用启动时自动加载初始数据到MongoDB
 */
@Configuration
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Bean
    CommandLineRunner initDatabase(VoucherRepository voucherRepository) {
        return args -> {
            // 检查是否已经有数据
            long count = voucherRepository.count();
            if (count > 0) {
                logger.info("数据库已存在 {} 张优惠券，跳过初始化", count);
                return;
            }
            
            logger.info("开始初始化优惠券数据...");
            
            List<Voucher> vouchers = Arrays.asList(
                // === 咖啡店优惠券 ===
                new Voucher("v1", "Starbucks UTown $5 Off", 450, "#00704A", "☕", 
                    "Valid at Starbucks UTown location. Min spend $8. Enjoy your favorite coffee!"),
                new Voucher("v2", "Starbucks YIH $5 Off", 450, "#00704A", "☕", 
                    "Valid at Starbucks Yusof Ishak House. Min spend $8. Perfect for study breaks!"),
                new Voucher("v3", "Coffee Bean $4 Off", 380, "#6F4E37", "☕", 
                    "Valid at The Coffee Bean & Tea Leaf at Science canteen. Min spend $6."),
                new Voucher("v4", "Toast Box Set Meal", 320, "#D2691E", "🍞", 
                    "Free coffee with any kaya toast set. Valid at UTown Toast Box."),
                new Voucher("v5", "Arise & Shine 15% Off", 280, "#90EE90", "🥗", 
                    "15% off total bill at Arise & Shine Café. Fresh salads and healthy options!"),
                
                // === 餐厅/食堂优惠券 ===
                new Voucher("v6", "The Deck $3 Off", 350, "#FF6347", "🍜", 
                    "Valid at The Deck (Engineering canteen). Min spend $5. Variety of Asian cuisine!"),
                new Voucher("v7", "Flavours @ UTown $5 Off", 500, "#F97316", "🍲", 
                    "Valid at Flavours food court. Choose from 15+ stalls. Min spend $8."),
                new Voucher("v8", "Hwang's Korean 20% Off", 600, "#FF1493", "🍱", 
                    "20% discount at Hwang's Korean Restaurant. Authentic Korean food at UTown!"),
                new Voucher("v9", "Subway 6-inch Sub", 420, "#FFC72C", "🥪", 
                    "Free 6-inch sub with drink purchase. Valid at Science & UTown outlets."),
                new Voucher("v10", "Fine Food $4 Off", 380, "#8B4513", "🍝", 
                    "Valid at Fine Food (Central Library). Great Western & Asian fusion dishes."),
                new Voucher("v11", "Platypus Food Bar $5 Off", 480, "#4682B4", "🍔", 
                    "Valid at Platypus (UTown). Gourmet burgers and pasta. Min spend $10."),
                new Voucher("v12", "Koufu Food Court $3 Off", 300, "#32CD32", "🍜", 
                    "Valid at any Koufu outlet on campus. Multiple food options available!"),
                
                // === 快餐/便利店 ===
                new Voucher("v13", "McDonald's Combo Meal", 520, "#FFC72C", "🍟", 
                    "Free upsize for any combo meal. Valid at Kent Ridge & Clementi outlets."),
                new Voucher("v14", "7-Eleven $2 Off", 180, "#008000", "🏪", 
                    "Valid at any NUS 7-Eleven store. Min spend $5."),
                new Voucher("v15", "Cheers $2 Off", 180, "#FF6B6B", "🏪", 
                    "Valid at Cheers convenience stores on campus. Min spend $5."),
                
                // === 特色餐饮 ===
                new Voucher("v16", "Waa Cow! Burrito Bowl", 400, "#D2691E", "🌯", 
                    "Free drink with burrito bowl purchase. Fresh Mexican food at UTown!"),
                new Voucher("v17", "Maki-San Free Topping", 280, "#FF69B4", "🍣", 
                    "Add 2 free premium toppings to your sushi roll. At Techno Edge."),
                new Voucher("v18", "Stuff'd Kebab Combo", 380, "#DAA520", "🥙", 
                    "Free side with any kebab. Valid at UTown Stuff'd outlet."),
                new Voucher("v19", "Old Chang Kee $3 Off", 250, "#FF8C00", "🥟", 
                    "Valid at Old Chang Kee (Science). Perfect for quick snacks!"),
                
                // === 饮品专门店 ===
                new Voucher("v20", "Gong Cha Pearl Tea", 320, "#DC2626", "🧋", 
                    "Buy 1 Get 1 Free on selected drinks. Valid at UTown Gong Cha."),
                new Voucher("v21", "Each A Cup Bubble Tea", 300, "#9C27B0", "🧋", 
                    "Free topping with any drink. Valid at COM2 Each A Cup."),
                new Voucher("v22", "Mr Bean Soya Drink", 200, "#8B4513", "🥤", 
                    "Free pancake with any soya drink. Various campus locations."),
                
                // === 外卖平台优惠券 ===
                new Voucher("v23", "Grab $10 Voucher", 800, "#00B14F", "🚗", 
                    "$10 credit for Grab rides. Perfect for getting around Singapore!"),
                
                // === 其他服务 ===
                new Voucher("v24", "NUS Co-op 15% Off", 450, "#3B82F6", "📚", 
                    "15% discount on books and stationery at NUS Co-op Bookstore."),
                new Voucher("v25", "Print & Copy $5 Credit", 400, "#6366F1", "🖨️", 
                    "Valid at any campus printing service. Great for assignments!"),
                new Voucher("v26", "MPSH Gym Pass", 350, "#EF4444", "🏋️", 
                    "Free day pass to Multi-Purpose Sports Hall gym facilities.")
            );
            
            voucherRepository.saveAll(vouchers);
            logger.info("成功初始化 {} 张优惠券数据", vouchers.size());
        };
    }
}
