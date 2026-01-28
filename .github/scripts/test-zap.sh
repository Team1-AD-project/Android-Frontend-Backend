#!/bin/bash
# OWASP ZAP 本地测试脚本
# 用于在本地环境测试 ZAP 扫描配置

set -e

echo "========================================="
echo "OWASP ZAP DAST 本地测试"
echo "========================================="

# 配置变量
TARGET_URL="${1:-http://localhost:8090}"
REPORT_DIR="./zap-reports"

echo "目标 URL: $TARGET_URL"
echo "报告目录: $REPORT_DIR"

# 创建报告目录
mkdir -p "$REPORT_DIR"

# 步骤 1: 检查目标应用是否运行
echo ""
echo "步骤 1/4: 检查目标应用..."
if curl -sf "$TARGET_URL/actuator/health" > /dev/null; then
    echo "✓ 应用正在运行"
else
    echo "✗ 应用未运行或不可达"
    echo "请先启动应用：java -jar target/EcoGo-*.jar"
    exit 1
fi

# 步骤 2: 运行 ZAP 基线扫描
echo ""
echo "步骤 2/4: 运行 ZAP 基线扫描..."
docker run --rm --network="host" -v "$(pwd):/zap/wrk/:rw" \
    -t zaproxy/zap-stable zap-baseline.py \
    -t "$TARGET_URL" \
    -g gen.conf \
    -r "$REPORT_DIR/baseline-report.html" \
    -w "$REPORT_DIR/baseline-report.md" \
    -J "$REPORT_DIR/baseline-report.json" || echo "基线扫描完成（可能有告警）"

# 步骤 3: 运行 ZAP 完整扫描（使用配置文件）
echo ""
echo "步骤 3/4: 运行 ZAP 完整扫描..."
if [ -f ".zap/zap-config.yaml" ]; then
    docker run --rm --network="host" -v "$(pwd):/zap/wrk/:rw" \
        -t zaproxy/zap-stable zap-full-scan.py \
        -t "$TARGET_URL" \
        -c .zap/zap-config.yaml \
        -r "$REPORT_DIR/full-report.html" \
        -w "$REPORT_DIR/full-report.md" \
        -J "$REPORT_DIR/full-report.json" || echo "完整扫描完成（可能有告警）"
else
    echo "⚠ 未找到 .zap/zap-config.yaml，跳过完整扫描"
fi

# 步骤 4: 生成报告摘要
echo ""
echo "步骤 4/4: 生成报告摘要..."
echo "========================================="
echo "扫描报告已生成"
echo "========================================="
ls -lh "$REPORT_DIR"/*.html 2>/dev/null || echo "未找到 HTML 报告"
ls -lh "$REPORT_DIR"/*.md 2>/dev/null || echo "未找到 Markdown 报告"
ls -lh "$REPORT_DIR"/*.json 2>/dev/null || echo "未找到 JSON 报告"

echo ""
echo "✓ ZAP 扫描测试完成！"
echo ""
echo "查看报告："
echo "  - 基线扫描: $REPORT_DIR/baseline-report.html"
echo "  - 完整扫描: $REPORT_DIR/full-report.html"
echo ""
echo "在浏览器中打开报告："
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "  open $REPORT_DIR/baseline-report.html"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "  xdg-open $REPORT_DIR/baseline-report.html"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    echo "  start $REPORT_DIR/baseline-report.html"
fi
