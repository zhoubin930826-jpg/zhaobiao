. "$PSScriptRoot\_script-common.ps1"

$name = "frontend"
$pidFile = Get-PidFilePath -Name $name
$logFile = Get-LogFilePath -Name $name
$port = 5173
$frontendDir = Join-Path $Script:ProjectRoot "frontend"
$nodeModules = Join-Path $frontendDir "node_modules"

$existing = Get-LiveProcessByPidFile -PidFile $pidFile
if ($existing) {
    Write-WarnMessage "Frontend is already running. PID=$($existing.Id)"
    exit 0
}

$portOwner = Get-PortOwnerPid -Port $port
if ($portOwner) {
    $commandLine = Get-ProcessCommandLine -ProcessId $portOwner
    throw "Port $port is already in use by PID $portOwner. $commandLine"
}

if (-not (Test-Path -LiteralPath $nodeModules)) {
    Write-Info "Installing frontend dependencies..."
    Push-Location $frontendDir
    try {
        npm install
    } finally {
        Pop-Location
    }
}

Write-Info "Starting frontend..."
$process = Start-BackgroundShellCommand `
    -WorkingDirectory $frontendDir `
    -Command "npm run dev -- --host 0.0.0.0" `
    -LogFile $logFile `
    -PidFile $pidFile

if (-not (Wait-HttpReady -Url "http://localhost:$port" -TimeoutSeconds 90)) {
    Write-WarnMessage "Frontend did not become ready in time. Recent log output:"
    Show-LogTail -LogFile $logFile
    throw "Frontend startup failed."
}

Write-Ok "Frontend is ready."
Write-Host "URL: http://localhost:$port/"
Write-Host "PID: $($process.Id)"
Write-Host "Log: $logFile"
