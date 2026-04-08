. "$PSScriptRoot\_script-common.ps1"

$name = "backend"
$pidFile = Get-PidFilePath -Name $name
$port = 8080

$process = Get-LiveProcessByPidFile -PidFile $pidFile
if ($process) {
    Write-Info "Stopping backend process tree. PID=$($process.Id)"
    Stop-ProcessTree -ProcessId $process.Id
    Remove-PidFile -PidFile $pidFile
    [void](Wait-PortClosed -Port $port -TimeoutSeconds 30)
    Write-Ok "Backend stopped."
    exit 0
}

$portOwner = Get-PortOwnerPid -Port $port
if (-not $portOwner) {
    Write-WarnMessage "Backend is not running."
    exit 0
}

$commandLine = Get-ProcessCommandLine -ProcessId $portOwner
if ($commandLine -like "*zhaobiao*" -or $commandLine -like "*spring-boot:run*") {
    Write-Info "Stopping backend process found by port. PID=$portOwner"
    Stop-ProcessTree -ProcessId $portOwner
    Remove-PidFile -PidFile $pidFile
    [void](Wait-PortClosed -Port $port -TimeoutSeconds 30)
    Write-Ok "Backend stopped."
    exit 0
}

throw "Port $port is in use by another process. PID=$portOwner. $commandLine"
