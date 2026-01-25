package com.example.EcoGo.repository;
import com.example.EcoGo.model.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends MongoRepository<Badge, String> {

    /**
     * 根据业务ID查找徽章
     * 用途：自动解锁逻辑中，根据 badgeId 获取规则
     */
    Optional<Badge> findByBadgeId(String badgeId);

    /**
     * 查找所有激活状态的徽章
     * 用途：自动检测时，只检查当前有效的徽章
     */
    List<Badge> findByIsActive(boolean isActive);
}