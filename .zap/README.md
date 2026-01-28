# OWASP ZAP DAST 配置

## 概述

此目录包含 OWASP ZAP（Zed Attack Proxy）动态应用安全测试（DAST）的配置文件。

## 文件说明

### zap-config.yaml
ZAP 自动化扫描的主配置文件，定义了：
- **扫描上下文**：目标 URL 和路径规则
- **扫描作业**：Spider、被动扫描、主动扫描
- **报告生成**：HTML 格式的安全报告

### rules.tsv
自定义扫描规则文件（可选），用于：
- 定义忽略特定警告
- 调整风险级别
- 自定义扫描行为

## CI/CD 集成

### 扫描流程

1. **基线扫描**（快速）
   ```bash
   zap-baseline.py -t http://localhost:8090 -r zap-report.html
   ```
   - 运行时间：~2-5 分钟
   - 仅被动扫描
   - 适合每次构建

2. **完整扫描**（深度）
   ```bash
   zap-full-scan.py -t http://localhost:8090 -c .zap/zap-config.yaml
   ```
   - 运行时间：~5-15 分钟
   - 包含主动扫描
   - 适合发布前检查

### 报告输出

扫描完成后会生成以下报告：
- `zap-report.html` - HTML 格式详细报告
- `zap-report.md` - Markdown 格式摘要
- `zap-report.json` - JSON 格式（可集成其他工具）
- `zap-report.xml` - XML 格式

## 本地运行

### 使用 Docker

```bash
# 基线扫描
docker run -v $(pwd):/zap/wrk/:rw -t zaproxy/zap-stable \
  zap-baseline.py -t http://localhost:8090 -r zap-report.html

# 完整扫描（使用配置文件）
docker run -v $(pwd):/zap/wrk/:rw -t zaproxy/zap-stable \
  zap-full-scan.py -t http://localhost:8090 \
  -c .zap/zap-config.yaml -r zap-report.html
```

### 使用 ZAP 桌面应用

1. 下载并安装 [OWASP ZAP](https://www.zaproxy.org/download/)
2. 启动应用程序
3. 导入 `zap-config.yaml` 配置
4. 配置目标 URL：http://localhost:8090
5. 运行自动化扫描

## 配置调整

### 缩短扫描时间

编辑 `zap-config.yaml`：
```yaml
- type: spider
  parameters:
    maxDuration: 2  # 减少爬取时间
    
- type: activeScan
  parameters:
    maxDuration: 3  # 减少主动扫描时间
```

### 排除特定路径

```yaml
excludePaths:
  - http://localhost:8090/actuator/.*
  - http://localhost:8090/static/.*
  - http://localhost:8090/public/.*
```

### 调整风险阈值

在 CI/CD 中设置失败条件：
```bash
zap-baseline.py -t http://localhost:8090 \
  -l PASS  # 仅高风险问题导致失败
```

风险级别：
- `PASS` - 仅 FAIL 级别问题会导致构建失败
- `WARN` - WARN 及以上级别问题会导致失败
- `FAIL` - 所有问题都会导致失败

## 常见问题

### 1. 扫描失败但未生成报告

**原因**：应用未启动或网络不可达

**解决**：
```bash
# 检查应用是否运行
curl http://localhost:8090/actuator/health

# 检查 Docker 网络
docker run --network="host" ...
```

### 2. 误报过多

**解决**：创建 `rules.tsv` 文件来忽略误报

### 3. 扫描时间过长

**解决**：
- 减少 `maxDuration` 参数
- 使用基线扫描而非完整扫描
- 限制扫描深度：`maxChildren: 10`

## 安全最佳实践

1. **定期扫描**：每次部署前运行 DAST 扫描
2. **审查报告**：不要忽略警告，即使是低风险
3. **修复优先级**：高 > 中 > 低
4. **跟踪趋势**：比较历史报告，确保安全性改善
5. **集成 SAST**：结合静态分析（SpotBugs、SonarQube）

## 更多资源

- [OWASP ZAP 文档](https://www.zaproxy.org/docs/)
- [ZAP 自动化框架](https://www.zaproxy.org/docs/automate/)
- [ZAP API 文档](https://www.zaproxy.org/docs/api/)
