# OWASP ZAP 本地测试脚本 (PowerShell)
# 用于在 Windows 环境测试 ZAP 扫描配置

param(
    [string]$TargetUrl = "http://localhost:8090"
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "OWASP ZAP DAST 本地测试" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$ReportDir = "./zap-reports"

Write-Host "目标 URL: $TargetUrl"
Write-Host "报告目录: $ReportDir"

# 创建报告目录
New-Item -ItemType Directory -Force -Path $ReportDir | Out-Null

# 步骤 1: 检查目标应用是否运行
Write-Host ""
Write-Host "步骤 1/4: 检查目标应用..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$TargetUrl/actuator/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "✓ 应用正在运行" -ForegroundColor Green
}
catch {
    Write-Host "✗ 应用未运行或不可达" -ForegroundColor Red
    Write-Host "请先启动应用：java -jar target/EcoGo-*.jar"
    exit 1
}

# 步骤 2: 运行 ZAP 基线扫描
Write-Host ""
Write-Host "步骤 2/4: 运行 ZAP 基线扫描..." -ForegroundColor Yellow
docker run --rm --network="host" -v "${PWD}:/zap/wrk/:rw" `
    -t zaproxy/zap-stable zap-baseline.py `
    -t $TargetUrl `
    -g gen.conf `
    -r "$ReportDir/baseline-report.html" `
    -w "$ReportDir/baseline-report.md" `
    -J "$ReportDir/baseline-report.json"
    
if ($LASTEXITCODE -ne 0) {
    Write-Host "基线扫描完成（可能有告警）" -ForegroundColor Yellow
}

# 步骤 3: 运行 ZAP 完整扫描（使用配置文件）
Write-Host ""
Write-Host "步骤 3/4: 运行 ZAP 完整扫描..." -ForegroundColor Yellow
if (Test-Path ".zap/zap-config.yaml") {
    docker run --rm --network="host" -v "${PWD}:/zap/wrk/:rw" `
        -t zaproxy/zap-stable zap-full-scan.py `
        -t $TargetUrl `
        -c .zap/zap-config.yaml `
        -r "$ReportDir/full-report.html" `
        -w "$ReportDir/full-report.md" `
        -J "$ReportDir/full-report.json"
        
    if ($LASTEXITCODE -ne 0) {
        Write-Host "完整扫描完成（可能有告警）" -ForegroundColor Yellow
    }
}
else {
    Write-Host "⚠ 未找到 .zap/zap-config.yaml，跳过完整扫描" -ForegroundColor Yellow
}

# 步骤 4: 生成报告摘要
Write-Host ""
Write-Host "步骤 4/4: 生成报告摘要..." -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "扫描报告已生成" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Get-ChildItem -Path $ReportDir -Filter "*.html" -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "$($_.Name) - $([math]::Round($_.Length/1KB, 2)) KB"
}

Write-Host ""
Write-Host "✓ ZAP 扫描测试完成！" -ForegroundColor Green
Write-Host ""
Write-Host "查看报告："
Write-Host "  - 基线扫描: $ReportDir/baseline-report.html"
Write-Host "  - 完整扫描: $ReportDir/full-report.html"
Write-Host ""
Write-Host "在浏览器中打开报告："
Write-Host "  start $ReportDir\baseline-report.html" -ForegroundColor Cyan
