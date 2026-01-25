package com.example.EcoGo.repository;
import com.example.EcoGo.model.UserBadge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends MongoRepository<UserBadge, String> {

    /**
     * 查询某个用户的所有已解锁徽章
     * 用途：APP端展示“我的徽章墙”
     */
    List<UserBadge> findByUserId(String userId);

    /**
     * 检查用户是否已经拥有某个徽章
     * 用途：自动解锁逻辑（防止重复解锁）
     */
    boolean existsByUserIdAndBadgeId(String userId, String badgeId);

    /**
     * 查找特定的用户徽章记录
     * 用途：用户点击“领取奖励”或“佩戴”时，先查出来再修改状态
     */
    Optional<UserBadge> findByUserIdAndBadgeId(String userId, String badgeId);
    
    /**
     * 统计用户拥有的徽章总数
     * 用途：个人主页展示 "已获得 5 枚徽章"
     */
    long countByUserId(String userId);
}
