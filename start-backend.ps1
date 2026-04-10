param(
    [string]$Profile = ""
)

. "$PSScriptRoot\_script-common.ps1"

$name = "backend"
$pidFile = Get-PidFilePath -Name $name
$logFile = Get-LogFilePath -Name $name
$port = 8080

$existing = Get-LiveProcessByPidFile -PidFile $pidFile
if ($existing) {
    Write-WarnMessage "Backend is already running. PID=$($existing.Id)"
    exit 0
}

$portOwner = Get-PortOwnerPid -Port $port
if ($portOwner) {
    $commandLine = Get-ProcessCommandLine -ProcessId $portOwner
    throw "Port $port is already in use by PID $portOwner. $commandLine"
}

$profileLabel = if ([string]::IsNullOrWhiteSpace($Profile)) { "default" } else { $Profile }
$startCommand = if ([string]::IsNullOrWhiteSpace($Profile)) {
    "mvn spring-boot:run"
} else {
    "mvn spring-boot:run -Dspring-boot.run.profiles=$Profile"
}

Write-Info "Starting backend with profile '$profileLabel'..."
$process = Start-BackgroundShellCommand `
    -WorkingDirectory $Script:ProjectRoot `
    -Command $startCommand `
    -LogFile $logFile `
    -PidFile $pidFile

if (-not (Wait-HttpReady -Url "http://localhost:$port/swagger-ui.html" -TimeoutSeconds 120)) {
    Write-WarnMessage "Backend did not become ready in time. Recent log output:"
    Show-LogTail -LogFile $logFile
    throw "Backend startup failed."
}

Write-Ok "Backend is ready."
Write-Host "Profile: $profileLabel"
Write-Host "URL: http://localhost:$port/"
Write-Host "Swagger: http://localhost:$port/swagger-ui.html"
if ($profileLabel -eq "preview") {
    Write-Host "H2 Console: http://localhost:$port/h2-console"
}
Write-Host "PID: $($process.Id)"
Write-Host "Log: $logFile"
