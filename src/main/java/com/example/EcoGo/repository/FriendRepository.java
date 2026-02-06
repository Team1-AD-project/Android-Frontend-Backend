package com.example.EcoGo.repository;

import com.example.EcoGo.model.Friend;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends MongoRepository<Friend, String> {
    List<Friend> findByUserIdAndStatus(String userId, String status);
    List<Friend> findByUserId(String userId);
    Optional<Friend> findByUserIdAndFriendId(String userId, String friendId);
    long countByUserIdAndStatus(String userId, String status);
    boolean existsByUserIdAndFriendId(String userId, String friendId);
}
