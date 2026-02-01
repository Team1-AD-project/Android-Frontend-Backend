package com.example.EcoGo.interfacemethods;

import com.example.EcoGo.model.Friend;
import com.example.EcoGo.model.FriendActivity;
import java.util.List;

public interface FriendInterface {
    Friend addFriend(String userId, String friendId);
    void removeFriend(String userId, String friendId);
    List<Friend> getUserFriends(String userId);
    List<Friend> getFriendRequests(String userId);
    Friend acceptFriendRequest(String userId, String friendId);
    List<FriendActivity> getFriendActivities(String userId);
    void recordFriendActivity(String userId, String friendId, String action, String details);
}
