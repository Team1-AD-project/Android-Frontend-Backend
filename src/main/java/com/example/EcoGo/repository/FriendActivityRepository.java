package com.example.EcoGo.repository;

import com.example.EcoGo.model.FriendActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FriendActivityRepository extends MongoRepository<FriendActivity, String> {
    List<FriendActivity> findByUserIdOrderByTimestampDesc(String userId);
    List<FriendActivity> findByFriendIdOrderByTimestampDesc(String friendId);
}
