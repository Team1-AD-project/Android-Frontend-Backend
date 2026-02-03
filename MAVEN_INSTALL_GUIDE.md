# 📦 Maven 安装指南

## 🚀 方法 1: 使用自动安装脚本（最简单）⭐

### 步骤：

1. **找到安装脚本**
   - 文件位置：`C:\Users\csls\Desktop\ad-ui\install-maven.bat`

2. **以管理员身份运行**
   - 右键点击 `install-maven.bat`
   - 选择 **"以管理员身份运行"**
   - 等待安装完成

3. **重新打开终端**
   - 关闭所有 PowerShell/CMD 窗口
   - 打开新的终端
   - 验证安装：`mvn -version`

4. **启动后端**
   ```cmd
   cd C:\Users\csls\Desktop\ad-ui
   mvn spring-boot:run
   ```

---

## 🛠️ 方法 2: 手动安装 Maven

### 步骤 1: 下载 Maven

1. 访问 Maven 官网：https://maven.apache.org/download.cgi
2. 下载 **Binary zip archive**
   - 文件名类似：`apache-maven-3.9.9-bin.zip`
3. 保存到下载文件夹

### 步骤 2: 解压 Maven

1. 右键点击下载的 zip 文件
2. 选择 "全部解压缩"
3. 解压到：`C:\Program Files\Apache\maven`
   - 完整路径示例：`C:\Program Files\Apache\maven\apache-maven-3.9.9`

### 步骤 3: 设置环境变量

#### 3.1 设置 MAVEN_HOME

1. 右键点击 **"此电脑"** > **"属性"**
2. 点击 **"高级系统设置"**
3. 点击 **"环境变量"**
4. 在 **"系统变量"** 区域，点击 **"新建"**
5. 输入：
   - **变量名：** `MAVEN_HOME`
   - **变量值：** `C:\Program Files\Apache\maven\apache-maven-3.9.9`
   - （根据您的实际路径调整）
6. 点击 **"确定"**

#### 3.2 添加到 PATH

1. 在 **"系统变量"** 中，找到并选中 **"Path"**
2. 点击 **"编辑"**
3. 点击 **"新建"**
4. 输入：`%MAVEN_HOME%\bin`
5. 点击 **"确定"**
6. 再次点击 **"确定"** 关闭所有窗口

### 步骤 4: 验证安装

1. **关闭所有终端窗口**（必须！）
2. 打开新的 PowerShell 或 CMD
3. 运行：
   ```cmd
   mvn -version
   ```
4. 应该看到类似输出：
   ```
   Apache Maven 3.9.9
   Maven home: C:\Program Files\Apache\maven\apache-maven-3.9.9
   Java version: 17, vendor: Oracle Corporation
   ```

---

## 🎯 方法 3: 使用 Chocolatey

### 前提条件：
- 需要管理员权限

### 步骤：

1. **以管理员身份打开 PowerShell**
   - 右键点击 Windows 图标
   - 选择 **"Windows PowerShell (管理员)"**

2. **安装 Chocolatey**（如果未安装）
   ```powershell
   Set-ExecutionPolicy Bypass -Scope Process -Force
   [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
   iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
   ```

3. **安装 Maven**
   ```powershell
   choco install maven -y
   ```

4. **重启终端并验证**
   ```cmd
   mvn -version
   ```

---

## ✅ 安装成功后的操作

### 1. 启动 EcoGo 后端

**选项 A: 使用 Maven 命令**
```cmd
cd C:\Users\csls\Desktop\ad-ui
mvn spring-boot:run
```

**选项 B: 使用 Maven Wrapper**
```cmd
cd C:\Users\csls\Desktop\ad-ui
.\mvnw.cmd spring-boot:run
```

### 2. 验证后端运行

等待看到类似输出：
```
Started EcoGoApplication in 3.5 seconds
```

### 3. 测试 API

浏览器访问：
```
http://localhost:8090/actuator/health
```

应该返回：
```json
{
  "status": "UP"
}
```

---

## 🔧 常见问题

### 问题 1: mvn 命令未找到

**原因：** 环境变量未生效

**解决方案：**
1. 完全关闭所有终端窗口
2. 重新打开新的终端
3. 如果仍然不行，重启电脑

### 问题 2: JAVA_HOME 错误

**症状：**
```
ERROR: JAVA_HOME is not set
```

**解决方案：**
1. 设置 JAVA_HOME 环境变量
2. 变量值：`C:\Program Files\Java\jdk-17`（根据实际路径调整）
3. 添加到 PATH：`%JAVA_HOME%\bin`

### 问题 3: 权限被拒绝

**症状：**
```
Access denied
```

**解决方案：**
- 以管理员身份运行 PowerShell/CMD

### 问题 4: 下载依赖失败

**症状：**
```
Failed to download artifact
```

**解决方案：**

**选项 A: 配置国内镜像**

编辑文件：`C:\Users\csls\.m2\settings.xml`

如果文件不存在，创建它并添加：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

**选项 B: 检查网络连接**
- 确保可以访问互联网
- 检查防火墙设置

---

## 📞 需要帮助？

如果遇到问题，请提供：
1. 完整的错误信息
2. `mvn -version` 的输出
3. `java -version` 的输出
4. 环境变量 PATH 的内容

---

## 🎉 快速参考

### 常用 Maven 命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包项目
mvn package

# 清理项目
mvn clean

# 运行 Spring Boot 应用
mvn spring-boot:run

# 跳过测试打包
mvn package -DskipTests
```

### Maven 配置文件位置

- **全局配置：** `C:\Program Files\Apache\maven\apache-maven-3.9.9\conf\settings.xml`
- **用户配置：** `C:\Users\csls\.m2\settings.xml`
- **项目配置：** `C:\Users\csls\Desktop\ad-ui\pom.xml`

---

## 🔗 有用的链接

- Maven 官网：https://maven.apache.org/
- Maven 下载：https://maven.apache.org/download.cgi
- Maven 文档：https://maven.apache.org/guides/
- Chocolatey：https://chocolatey.org/
- Spring Boot Maven 插件：https://docs.spring.io/spring-boot/maven-plugin/
