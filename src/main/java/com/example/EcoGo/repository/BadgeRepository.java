package com.example.EcoGo.repository;
import com.example.EcoGo.model.Badge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends MongoRepository<Badge, String> {

 Optional<Badge> findByBadgeId(String badgeId);
    
    List<Badge> findByIsActive(boolean isActive);

    // ✅ 必须有这个方法，Service 才能查出同类的其他徽章
    List<Badge> findByCategory(String category);
}