# Fix Maven Wrapper Script
Write-Host "正在修复 Maven Wrapper..." -ForegroundColor Green

$wrapperDir = ".mvn\wrapper"
$wrapperJarUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
$wrapperJarPath = "$wrapperDir\maven-wrapper.jar"

# 确保目录存在
if (-not (Test-Path $wrapperDir)) {
    Write-Host "创建 wrapper 目录..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
}

# 下载 maven-wrapper.jar
Write-Host "下载 maven-wrapper.jar..." -ForegroundColor Yellow
try {
    Invoke-WebRequest -Uri $wrapperJarUrl -OutFile $wrapperJarPath
    Write-Host "✅ Maven Wrapper 已修复！" -ForegroundColor Green
    Write-Host ""
    Write-Host "现在可以运行：" -ForegroundColor Cyan
    Write-Host "  .\mvnw.cmd spring-boot:run" -ForegroundColor Yellow
} catch {
    Write-Host "❌ 下载失败: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "请尝试手动下载：" -ForegroundColor Yellow
    Write-Host "  1. 访问: $wrapperJarUrl"
    Write-Host "  2. 保存到: $wrapperJarPath"
}
