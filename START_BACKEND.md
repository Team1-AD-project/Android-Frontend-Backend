# 🚀 后端启动指南

## ⚠️ 当前问题

Maven Wrapper 遇到了 PowerShell 执行策略问题。以下是 3 种解决方案：

---

## ✅ 方案 1: 使用 IntelliJ IDEA（最简单，推荐）⭐

### 步骤：

1. **打开 IntelliJ IDEA**

2. **打开项目**
   - File > Open
   - 选择文件夹: `C:\Users\csls\Desktop\ad-ui`
   - 点击 OK

3. **等待项目加载**
   - IDEA 会自动识别 Maven 项目
   - 等待右下角的索引和依赖下载完成
   - 如果提示 "Maven projects need to be imported"，点击 "Import Changes"

4. **找到主类**
   - 导航到: `src/main/java/com/example/EcoGo/EcoGoApplication.java`

5. **运行**
   - 右键点击 `EcoGoApplication.java`
   - 选择 **"Run 'EcoGoApplication'"**
   - 或点击代码左侧的绿色 ▶️ 按钮

6. **等待启动**
   - 查看控制台输出
   - 等待看到: `Started EcoGoApplication in X.XXX seconds`

7. **验证**
   - 浏览器访问: http://localhost:8090/actuator/health
   - 应该返回: `{"status":"UP"}`

---

## ✅ 方案 2: 修复 PowerShell 执行策略

### 步骤：

1. **以管理员身份打开 PowerShell**
   - 右键点击 Windows 图标
   - 选择 "Windows PowerShell (管理员)"

2. **检查当前策略**
   ```powershell
   Get-ExecutionPolicy
   ```

3. **设置执行策略**
   ```powershell
   Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```
   - 提示时输入 `Y` 确认

4. **进入项目目录**
   ```powershell
   cd C:\Users\csls\Desktop\ad-ui
   ```

5. **启动后端**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

6. **等待启动**
   - 看到 "Started EcoGoApplication" 表示成功

7. **验证**
   - 浏览器访问: http://localhost:8090/actuator/health

---

## ✅ 方案 3: 手动安装 Maven

### 步骤：

1. **下载 Maven**
   - 访问: https://maven.apache.org/download.cgi
   - 下载: apache-maven-3.9.x-bin.zip

2. **解压**
   - 解压到: `C:\Program Files\Apache\maven`

3. **设置环境变量**
   - 右键 "此电脑" > 属性 > 高级系统设置 > 环境变量
   - 系统变量 > 新建:
     - 变量名: `MAVEN_HOME`
     - 变量值: `C:\Program Files\Apache\maven`
   - 编辑 `Path` 变量，添加: `%MAVEN_HOME%\bin`

4. **验证安装**
   - 打开新的命令提示符
   ```cmd
   mvn -version
   ```

5. **启动后端**
   ```cmd
   cd C:\Users\csls\Desktop\ad-ui
   mvn spring-boot:run
   ```

6. **验证**
   - 浏览器访问: http://localhost:8090/actuator/health

---

## 🔍 启动成功的标志

### 控制台输出（成功）：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.5.9)

2026-01-29 14:30:00 [main] INFO  c.e.EcoGo.EcoGoApplication - Starting EcoGoApplication
2026-01-29 14:30:01 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port 8090
2026-01-29 14:30:02 [main] INFO  o.s.d.m.c.MongoClient - Opened connection
2026-01-29 14:30:03 [main] INFO  c.e.EcoGo.EcoGoApplication - Started EcoGoApplication in 3.5 seconds
```

### 浏览器验证（成功）：

访问 http://localhost:8090/actuator/health

```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## ❌ 常见错误

### 错误 1: MongoDB 连接失败

**症状：**
```
Failed to connect to MongoDB at localhost:27017
```

**解决方案：**

1. **启动 MongoDB**
   ```cmd
   # 方法 1: 作为服务启动
   net start MongoDB

   # 方法 2: 手动启动
   mongod --dbpath C:\data\db
   ```

2. **验证 MongoDB 运行**
   ```cmd
   mongosh --eval "db.version()"
   ```

3. **如果 MongoDB 未安装**
   - 下载: https://www.mongodb.com/try/download/community
   - 安装并启动服务

---

### 错误 2: 端口 8090 被占用

**症状：**
```
Port 8090 is already in use
```

**解决方案：**

1. **查找占用进程**
   ```cmd
   netstat -ano | findstr :8090
   ```

2. **杀死进程**
   ```cmd
   taskkill /PID <进程ID> /F
   ```

3. **或修改端口**
   - 编辑 `src/main/resources/application.yaml`
   - 修改 `server.port: 8091`

---

### 错误 3: Java 版本不匹配

**症状：**
```
Unsupported class file major version 61
```

**解决方案：**

1. **检查 Java 版本**
   ```cmd
   java -version
   ```

2. **必须是 Java 17**
   - 下载: https://www.oracle.com/java/technologies/downloads/#java17
   - 或 OpenJDK: https://adoptium.net/

3. **设置 JAVA_HOME**
   - 环境变量 > 系统变量 > 新建
   - 变量名: `JAVA_HOME`
   - 变量值: `C:\Program Files\Java\jdk-17`

---

## 📊 测试 API 接口

### 使用浏览器测试：

```
http://localhost:8090/actuator/health
http://localhost:8090/api/v1/activities
http://localhost:8090/api/v1/leaderboards/periods
```

### 使用 curl 测试：

```powershell
# 健康检查
curl.exe http://localhost:8090/actuator/health

# 获取活动列表
curl.exe http://localhost:8090/api/v1/activities

# 获取排行榜周期
curl.exe http://localhost:8090/api/v1/leaderboards/periods
```

### 使用 Postman 测试：

1. 下载 Postman: https://www.postman.com/downloads/
2. 创建新请求
3. 方法: GET
4. URL: http://localhost:8090/api/v1/activities
5. 点击 Send

---

## 🎯 推荐流程

### 最快的方式：

1. ✅ 打开 IntelliJ IDEA
2. ✅ 打开项目: `C:\Users\csls\Desktop\ad-ui`
3. ✅ 等待 Maven 依赖下载完成
4. ✅ 右键 `EcoGoApplication.java` > Run
5. ✅ 等待启动完成
6. ✅ 浏览器访问: http://localhost:8090/actuator/health

### 预计时间：
- 首次启动: 3-5 分钟（下载依赖）
- 后续启动: 30-60 秒

---

## 📞 需要帮助？

如果遇到问题，请提供：
1. 完整的错误日志（控制台输出）
2. Java 版本 (`java -version`)
3. MongoDB 状态 (`net start MongoDB` 或 `mongosh`)
4. 端口占用情况 (`netstat -ano | findstr :8090`)

然后告诉我，我会帮你解决！😊
