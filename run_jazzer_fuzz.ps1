
param(
    [string]$TargetClass = "com.marketplace.fuzz.CombinedFuzzer",
    [string]$Duration = "5h",
    [string]$ReproducerPath = "jazzer-repro",
    [string]$LogFile = "jazzer_run.log"
)

Write-Host "== Jazzer fuzz runner ==" -ForegroundColor Cyan
Write-Host "TargetClass: $TargetClass"
Write-Host "Duration: $Duration"
Write-Host "ReproducerPath: $ReproducerPath"
Write-Host "LogFile: $LogFile"

# 1. 构建项目并跳过单元测试
Write-Host "[1/4] Running mvn -DskipTests package..." -ForegroundColor Green
mvn -DskipTests package
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed (exit code $LASTEXITCODE). Aborting."
    exit $LASTEXITCODE
}

# 2. 复制运行时依赖到 target/dependency
Write-Host "[2/4] Copying runtime dependencies to target/dependency..." -ForegroundColor Green
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime
if ($LASTEXITCODE -ne 0) {
    Write-Warning "Warning: copying dependencies failed or returned non-zero exit code ($LASTEXITCODE). Continue anyway."
}

# 3. 启动 Jazzer（覆盖写入日志）
Write-Host "[3/4] Starting Jazzer (logging to $LogFile)..." -ForegroundColor Green
$jazzerJar = "tools/jazzer/jazzer_standalone.jar"
if (-not (Test-Path $jazzerJar)) {
    Write-Error "Jazzer jar not found at $jazzerJar. Please place jazzer_standalone.jar in tools/jazzer/."
    exit 1
}

# 构造类路径（Windows 下使用分号）
$cp = "tools/jazzer/jazzer_standalone.jar;target/classes;target/test-classes;target/dependency/*"

$jazzerCmd = "java -cp `"$cp`" com.code_intelligence.jazzer.Jazzer --target_class=$TargetClass --instrumentation_includes=com.marketplace.* --reproducer_path=$ReproducerPath --max_duration=$Duration"

Write-Host "Running command:" -ForegroundColor Yellow
Write-Host $jazzerCmd

# 启动 Jazzer 并把 stdout/stderr 重定向到日志文件（同步运行，若需后台运行可在外部用 Start-Process）
& cmd /c "$jazzerCmd > $LogFile 2>&1"

Write-Host "Jazzer finished. Log: $LogFile" -ForegroundColor Cyan

# 4. 提示监控与复现器查看命令
Write-Host "查看日志尾部（实时）: Get-Content $LogFile -Tail 200 -Wait" -ForegroundColor Gray
Write-Host "查看复现器目录（如存在）: dir $ReproducerPath" -ForegroundColor Gray
Write-Host "如果需要在后台运行，请改用: Start-Process -FilePath 'powershell' -ArgumentList '-NoProfile -File .\run_jazzer_fuzz.ps1' -WindowStyle Hidden" -ForegroundColor Gray
