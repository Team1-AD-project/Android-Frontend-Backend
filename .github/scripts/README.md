# CI/CD 脚本工具集

本目录包含用于 CI/CD 流程的各种脚本和工具。

## 脚本列表

### 1. smoke-tests.sh
**用途**：运行基本的冒烟测试，验证应用程序的核心功能

**使用方法**：
```bash
./smoke-tests.sh <TARGET_URL>
```

**示例**：
```bash
./smoke-tests.sh http://localhost:8090
./smoke-tests.sh https://staging.example.com
```

### 2. test-zap.sh (Linux/Mac)
**用途**：在本地运行 OWASP ZAP 安全扫描测试

**使用方法**：
```bash
chmod +x test-zap.sh
./test-zap.sh [TARGET_URL]
```

**示例**：
```bash
./test-zap.sh http://localhost:8090
```

**输出**：
- `zap-reports/baseline-report.html` - 基线扫描报告
- `zap-reports/full-report.html` - 完整扫描报告

### 3. test-zap.ps1 (Windows)
**用途**：在 Windows 环境下运行 OWASP ZAP 安全扫描测试

**使用方法**：
```powershell
.\test-zap.ps1 -TargetUrl "http://localhost:8090"
```

**示例**：
```powershell
# 使用默认 URL
.\test-zap.ps1

# 指定自定义 URL
.\test-zap.ps1 -TargetUrl "http://localhost:8090"
```

### 4. validate-yaml.py
**用途**：验证 YAML 配置文件的语法正确性

**使用方法**：
```bash
python validate-yaml.py
```

**验证的文件**：
- `.github/workflows/cicd-pipeline.yml`
- `.zap/zap-config.yaml`

## 前置要求

### 所有脚本
- Git Bash（Windows）或 Bash shell（Linux/Mac）
- Docker（用于运行 ZAP 扫描）

### test-zap.sh / test-zap.ps1
- Docker Desktop 已安装并运行
- 目标应用程序正在运行

### validate-yaml.py
- Python 3.6+
- PyYAML 库：`pip install pyyaml`

## 常见使用场景

### 场景 1：部署前验证
```bash
# 1. 验证配置文件
python .github/scripts/validate-yaml.py

# 2. 启动应用
java -jar target/EcoGo-*.jar &

# 3. 运行冒烟测试
./.github/scripts/smoke-tests.sh http://localhost:8090

# 4. 运行安全扫描
./.github/scripts/test-zap.sh http://localhost:8090
```

### 场景 2：CI/CD 流程调试
```bash
# 验证 YAML 语法
python .github/scripts/validate-yaml.py

# 查看详细错误信息
cat /path/to/error.log
```

### 场景 3：本地安全测试
```bash
# 启动应用
java -jar target/EcoGo-*.jar

# 新终端窗口运行 ZAP 扫描
./.github/scripts/test-zap.sh

# 查看报告
open zap-reports/baseline-report.html  # Mac
xdg-open zap-reports/baseline-report.html  # Linux
start zap-reports/baseline-report.html  # Windows
```

## 故障排除

### 问题：Docker 容器无法连接到主机
**解决方案**：使用 `--network="host"` 参数（仅限 Linux）

**Windows/Mac**：
```bash
# 使用 host.docker.internal
docker run ... -t http://host.docker.internal:8090
```

### 问题：ZAP 扫描报告未生成
**可能原因**：
1. 应用程序未运行
2. 网络连接问题
3. Docker 卷挂载失败

**检查步骤**：
```bash
# 检查应用是否运行
curl http://localhost:8090/actuator/health

# 检查 Docker 卷挂载
docker run -v $(pwd):/zap/wrk/:rw ... ls /zap/wrk

# 手动创建报告目录
mkdir -p zap-reports
```

### 问题：YAML 验证失败
**解决方案**：
1. 检查缩进（使用空格，不用制表符）
2. 检查特殊字符（避免使用 emoji）
3. 验证引号匹配

## 贡献指南

添加新脚本时，请遵循以下规范：

1. **命名约定**：使用小写字母和连字符（`script-name.sh`）
2. **文档**：在文件顶部添加用途说明
3. **错误处理**：使用 `set -e` 并提供有意义的错误消息
4. **参数验证**：验证必需的参数
5. **帮助信息**：支持 `-h` 或 `--help` 参数

**示例模板**：
```bash
#!/bin/bash
# Script Name: example-script.sh
# Description: Brief description of what this script does
# Usage: ./example-script.sh <arg1> <arg2>

set -e

# Help message
if [ "$1" == "-h" ] || [ "$1" == "--help" ]; then
    echo "Usage: $0 <arg1> <arg2>"
    echo "Description: ..."
    exit 0
fi

# Validate arguments
if [ -z "$1" ]; then
    echo "Error: arg1 is required"
    exit 1
fi

# Script logic
echo "Running script..."
```

## 更多信息

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [OWASP ZAP 文档](https://www.zaproxy.org/docs/)
- [Docker 文档](https://docs.docker.com/)
