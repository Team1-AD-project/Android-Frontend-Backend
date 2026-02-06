package com.example.EcoGo.controller;

import com.example.EcoGo.dto.ResponseMessage;
import com.example.EcoGo.interfacemethods.FriendInterface;
import com.example.EcoGo.model.Friend;
import com.example.EcoGo.model.FriendActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 好友管理接口控制器
 * 路径规范：/api/v1/friends
 */
@RestController
@RequestMapping("/api/v1/friends")
public class FriendController {
    private static final Logger logger = LoggerFactory.getLogger(FriendController.class);

    @Autowired
    private FriendInterface friendService;

    /**
     * 获取好友列表
     * GET /api/v1/friends/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseMessage<List<Friend>> getUserFriends(@PathVariable String userId) {
        logger.info("获取好友列表，userID：{}", userId);
        List<Friend> friends = friendService.getUserFriends(userId);
        return ResponseMessage.success(friends);
    }

    /**
     * 添加好友
     * POST /api/v1/friends/add
     */
    @PostMapping("/add")
    public ResponseMessage<Friend> addFriend(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String friendId = request.get("friendId");
        logger.info("添加好友，userID：{}，friendID：{}", userId, friendId);
        
        Friend friend = friendService.addFriend(userId, friendId);
        return ResponseMessage.success(friend);
    }

    /**
     * 删除好友
     * DELETE /api/v1/friends/{userId}/{friendId}
     */
    @DeleteMapping("/{userId}/{friendId}")
    public ResponseMessage<Void> removeFriend(
            @PathVariable String userId,
            @PathVariable String friendId) {
        logger.info("删除好友，userID：{}，friendID：{}", userId, friendId);
        friendService.removeFriend(userId, friendId);
        return ResponseMessage.success(null);
    }

    /**
     * 获取好友请求
     * GET /api/v1/friends/requests/{userId}
     */
    @GetMapping("/requests/{userId}")
    public ResponseMessage<List<Friend>> getFriendRequests(@PathVariable String userId) {
        logger.info("获取好友请求，userID：{}", userId);
        List<Friend> requests = friendService.getFriendRequests(userId);
        return ResponseMessage.success(requests);
    }

    /**
     * 接受好友请求
     * POST /api/v1/friends/accept
     */
    @PostMapping("/accept")
    public ResponseMessage<Friend> acceptFriendRequest(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String friendId = request.get("friendId");
        logger.info("接受好友请求，userID：{}，friendID：{}", userId, friendId);
        
        Friend friend = friendService.acceptFriendRequest(userId, friendId);
        return ResponseMessage.success(friend);
    }

    /**
     * 获取好友动态
     * GET /api/v1/friends/{userId}/activities
     */
    @GetMapping("/{userId}/activities")
    public ResponseMessage<List<FriendActivity>> getFriendActivities(@PathVariable String userId) {
        logger.info("获取好友动态，userID：{}", userId);
        List<FriendActivity> activities = friendService.getFriendActivities(userId);
        return ResponseMessage.success(activities);
    }
}
