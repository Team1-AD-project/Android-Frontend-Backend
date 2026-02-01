package com.example.EcoGo.service;

import com.example.EcoGo.interfacemethods.FriendInterface;
import com.example.EcoGo.model.Friend;
import com.example.EcoGo.model.FriendActivity;
import com.example.EcoGo.model.User;
import com.example.EcoGo.repository.FriendRepository;
import com.example.EcoGo.repository.FriendActivityRepository;
import com.example.EcoGo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendImplementation implements FriendInterface {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private FriendActivityRepository friendActivityRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Friend addFriend(String userId, String friendId) {
        // 检查是否已经是好友
        if (friendRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new RuntimeException("Already friends or request pending");
        }

        // 创建好友请求
        Friend friend = new Friend(userId, friendId);
        friend.setStatus("pending");

        // 获取好友信息
        Optional<User> friendUser = userRepository.findById(friendId);
        if (friendUser.isPresent()) {
            User user = friendUser.get();
            friend.setFriendNickname(user.getNickname());
            friend.setFriendFaculty(user.getFaculty());
        }

        return friendRepository.save(friend);
    }

    @Override
    public void removeFriend(String userId, String friendId) {
        Optional<Friend> friend = friendRepository.findByUserIdAndFriendId(userId, friendId);
        friend.ifPresent(f -> friendRepository.delete(f));
    }

    @Override
    public List<Friend> getUserFriends(String userId) {
        List<Friend> friends = friendRepository.findByUserIdAndStatus(userId, "accepted");
        
        // 更新好友的最新信息
        return friends.stream().map(friend -> {
            Optional<User> friendUser = userRepository.findById(friend.getFriendId());
            if (friendUser.isPresent()) {
                User user = friendUser.get();
                friend.setFriendNickname(user.getNickname());
                friend.setFriendPoints(user.getTotalPoints());
                friend.setFriendFaculty(user.getFaculty());
            }
            return friend;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Friend> getFriendRequests(String userId) {
        return friendRepository.findByUserIdAndStatus(userId, "pending");
    }

    @Override
    public Friend acceptFriendRequest(String userId, String friendId) {
        Optional<Friend> friendOpt = friendRepository.findByUserIdAndFriendId(friendId, userId);
        if (friendOpt.isPresent()) {
            Friend friend = friendOpt.get();
            friend.setStatus("accepted");
            friend.setUpdatedAt(LocalDateTime.now());
            
            // 创建双向好友关系
            Friend reverseFriend = new Friend(userId, friendId);
            reverseFriend.setStatus("accepted");
            friendRepository.save(reverseFriend);
            
            return friendRepository.save(friend);
        }
        throw new RuntimeException("Friend request not found");
    }

    @Override
    public List<FriendActivity> getFriendActivities(String userId) {
        return friendActivityRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Override
    public void recordFriendActivity(String userId, String friendId, String action, String details) {
        Optional<User> friendUser = userRepository.findById(friendId);
        if (friendUser.isPresent()) {
            FriendActivity activity = new FriendActivity(
                    userId, friendId, friendUser.get().getNickname(), action, details);
            friendActivityRepository.save(activity);
        }
    }
}
