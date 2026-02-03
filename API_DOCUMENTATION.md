# EcoGo API 文档

## 基础信息
- **Base URL**: `http://localhost:8080/api/v1`
- **认证方式**: JWT Token (待实现)
- **响应格式**: JSON

## 响应结构

```json
{
  "success": true,
  "data": {},
  "message": "操作成功",
  "timestamp": "2026-01-31T10:00:00"
}
```

---

## 1. 签到管理 API

### 1.1 执行签到
```http
POST /checkin?userId={userId}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "success": true,
    "pointsEarned": 15,
    "consecutiveDays": 3,
    "message": "签到成功！"
  }
}
```

### 1.2 获取签到状态
```http
GET /checkin/status/{userId}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "userId": "user123",
    "lastCheckInDate": "2026-01-31",
    "consecutiveDays": 5,
    "totalCheckIns": 120,
    "pointsEarned": 20
  }
}
```

### 1.3 获取签到历史
```http
GET /checkin/history/{userId}
```

---

## 2. 每日目标 API

### 2.1 获取今日目标
```http
GET /goals/daily/{userId}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "id": "goal123",
    "userId": "user123",
    "date": "2026-01-31",
    "stepGoal": 10000,
    "currentSteps": 6800,
    "tripGoal": 3,
    "currentTrips": 2,
    "co2SavedGoal": 2.0,
    "currentCo2Saved": 1.5
  }
}
```

### 2.2 更新今日目标
```http
PUT /goals/daily/{userId}
Content-Type: application/json

{
  "currentSteps": 8000,
  "currentTrips": 2,
  "currentCo2Saved": 1.8
}
```

---

## 3. 碳足迹 API

### 3.1 获取碳足迹数据
```http
GET /carbon/{userId}?period=monthly
```

**参数**:
- `period`: daily, weekly, monthly

**响应示例**:
```json
{
  "success": true,
  "data": {
    "id": "carbon123",
    "userId": "user123",
    "period": "monthly",
    "startDate": "2026-01-01",
    "endDate": "2026-01-31",
    "co2Saved": 45.6,
    "equivalentTrees": 10,
    "tripsByBus": 25,
    "tripsByWalking": 15,
    "tripsByBicycle": 8
  }
}
```

### 3.2 记录出行
```http
POST /carbon/record
Content-Type: application/json

{
  "userId": "user123",
  "tripType": "bus",
  "distance": 5.2
}
```

**tripType**: bus, walking, bicycle

---

## 4. 通知管理 API

### 4.1 获取用户通知
```http
GET /notifications/{userId}
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": "notif123",
      "userId": "user123",
      "type": "activity",
      "title": "新活动邀请",
      "message": "您的好友邀请您参加"校园清洁日"",
      "isRead": false,
      "actionUrl": "/activities/act123",
      "createdAt": "2026-01-31T10:00:00"
    }
  ]
}
```

### 4.2 获取未读通知
```http
GET /notifications/{userId}/unread
```

### 4.3 创建通知
```http
POST /notifications
Content-Type: application/json

{
  "userId": "user123",
  "type": "activity",
  "title": "活动提醒",
  "message": "活动将在1小时后开始",
  "actionUrl": "/activities/act123"
}
```

### 4.4 标记通知为已读
```http
POST /notifications/{notificationId}/read
```

### 4.5 全部标记为已读
```http
POST /notifications/{userId}/read-all
```

---

## 5. 好友管理 API

### 5.1 获取好友列表
```http
GET /friends/{userId}
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": "friend123",
      "userId": "user123",
      "friendId": "user456",
      "friendNickname": "Alex Chen",
      "friendFaculty": "School of Computing",
      "friendPoints": 920,
      "friendRank": 1,
      "status": "accepted",
      "createdAt": "2026-01-15T10:00:00"
    }
  ]
}
```

### 5.2 添加好友
```http
POST /friends/add
Content-Type: application/json

{
  "userId": "user123",
  "friendId": "user456"
}
```

### 5.3 删除好友
```http
DELETE /friends/{userId}/{friendId}
```

### 5.4 获取好友请求
```http
GET /friends/requests/{userId}
```

### 5.5 接受好友请求
```http
POST /friends/accept
Content-Type: application/json

{
  "userId": "user123",
  "friendId": "user456"
}
```

### 5.6 获取好友动态
```http
GET /friends/{userId}/activities
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": "activity123",
      "userId": "user123",
      "friendId": "user456",
      "friendName": "Alex Chen",
      "action": "joined_activity",
      "details": "参加了"校园清洁日"活动",
      "timestamp": "2026-01-31T09:30:00"
    }
  ]
}
```

---

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源未找到 |
| 500 | 服务器内部错误 |

## 通知类型说明

| 类型 | 说明 |
|-----|------|
| activity | 活动相关 |
| bus_delay | 巴士延误 |
| achievement | 成就获得 |
| system | 系统通知 |

## 好友动作类型

| 动作 | 说明 |
|-----|------|
| joined_activity | 参加活动 |
| earned_badge | 获得徽章 |
| completed_goal | 完成目标 |
| level_up | 等级提升 |

---

**版本**: v1.0  
**更新日期**: 2026-01-31
