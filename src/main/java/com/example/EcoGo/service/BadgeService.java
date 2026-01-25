package com.example.EcoGo.service;

import com.example.EcoGo.model.Badge;
import com.example.EcoGo.model.User;
import com.example.EcoGo.model.UserBadge;
import com.example.EcoGo.repository.BadgeRepository;
import com.example.EcoGo.repository.UserBadgeRepository;
import com.example.EcoGo.repository.UserRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class BadgeService {

    @Autowired private BadgeRepository badgeRepository;
    @Autowired private UserBadgeRepository userBadgeRepository;
    @Autowired private UserRepository userRepository;

    /**
     * 1. 购买徽章
     */
    @Transactional
    public UserBadge purchaseBadge(String userId, String badgeId) {
        if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badgeId)) {
            throw new RuntimeException("您已拥有该徽章");
        }
        
        Badge badge = badgeRepository.findByBadgeId(badgeId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        int cost = badge.getPurchaseCost();
        if (cost <= 0) throw new RuntimeException("该徽章不可购买");

        User user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getTotalPoints() < cost) {
            throw new RuntimeException("积分不足");
        }

        user.setTotalPoints(user.getTotalPoints() - cost);
        userRepository.save(user);

        UserBadge newBadge = new UserBadge();
        newBadge.setUserId(userId);
        newBadge.setBadgeId(badgeId);
        newBadge.setUnlockedAt(new Date());
        newBadge.setDisplay(false); // 默认不佩戴
        newBadge.setCreatedAt(new Date());
        
        return userBadgeRepository.save(newBadge);
    }

    /**
     * 2. 切换佩戴状态 (支持同类互斥)
     * 逻辑：如果佩戴的是 "RANK" 类，系统会自动卸下用户身上已有的其他 "RANK" 类徽章
     */
    @Transactional
    public UserBadge toggleBadgeDisplay(String userId, String badgeId, boolean isDisplay) {
        
        UserBadge targetUserBadge = userBadgeRepository.findByUserIdAndBadgeId(userId, badgeId)
                .orElseThrow(() -> new RuntimeException("您还没有获得这个徽章"));

        // 如果是“取下”，直接操作，无需互斥
        if (!isDisplay) {
            targetUserBadge.setDisplay(false);
            return userBadgeRepository.save(targetUserBadge);
        }

        // ================= 同类互斥核心逻辑 =================
        
        // A. 查当前徽章的分类
        Badge targetBadgeDef = badgeRepository.findByBadgeId(badgeId)
                .orElseThrow(() -> new RuntimeException("徽章定义不存在"));
        
        String category = targetBadgeDef.getCategory(); // ✅ 现在这里不会报错了

        if (category != null && !category.isEmpty()) {
            // B. 找出该分类下所有的徽章ID (比如所有 RANK 类的)
            List<Badge> sameCategoryBadges = badgeRepository.findByCategory(category);
            List<String> sameCategoryBadgeIds = sameCategoryBadges.stream()
                    .map(Badge::getBadgeId)
                    .toList();

            // C. 找出用户身上正在戴着的、且属于该分类的旧徽章
            List<UserBadge> conflictingBadges = userBadgeRepository
                    .findByUserIdAndIsDisplayTrueAndBadgeIdIn(userId, sameCategoryBadgeIds);

            // D. 统统卸下
            for (UserBadge conflict : conflictingBadges) {
                if (!conflict.getBadgeId().equals(badgeId)) {
                    conflict.setDisplay(false);
                    userBadgeRepository.save(conflict);
                }
            }
        }
        // ====================================================

        targetUserBadge.setDisplay(true);
        return userBadgeRepository.save(targetUserBadge);
    }

    // ... 其他 getter 方法 (getShopList, getMyBadges) 保持不变 ...
    public List<Badge> getShopList() { return badgeRepository.findByIsActive(true); }
    public List<UserBadge> getMyBadges(String userId) { return userBadgeRepository.findByUserId(userId); }
    public Badge createBadge(Badge badge) { return badgeRepository.save(badge); }
}