Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Script:ProjectRoot = $PSScriptRoot
$Script:LogDir = Join-Path $Script:ProjectRoot "logs"
$Script:RunDir = Join-Path $Script:ProjectRoot ".run"

function Ensure-Directory {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path -Force | Out-Null
    }
}

Ensure-Directory -Path $Script:LogDir
Ensure-Directory -Path $Script:RunDir

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Write-WarnMessage {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Ok {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Get-PidFilePath {
    param([string]$Name)
    return (Join-Path $Script:RunDir "$Name.pid")
}

function Get-LogFilePath {
    param([string]$Name)
    return (Join-Path $Script:LogDir "$Name.log")
}

function Save-Pid {
    param(
        [string]$PidFile,
        [int]$ProcessId
    )

    Set-Content -Path $PidFile -Value $ProcessId -Encoding ASCII
}

function Remove-PidFile {
    param([string]$PidFile)

    if (Test-Path -LiteralPath $PidFile) {
        Remove-Item -LiteralPath $PidFile -Force -ErrorAction SilentlyContinue
    }
}

function Get-LiveProcessByPidFile {
    param([string]$PidFile)

    if (-not (Test-Path -LiteralPath $PidFile)) {
        return $null
    }

    $raw = Get-Content -LiteralPath $PidFile -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $raw -or $raw -notmatch "^\d+$") {
        Remove-PidFile -PidFile $PidFile
        return $null
    }

    try {
        return Get-Process -Id ([int]$raw) -ErrorAction Stop
    } catch {
        Remove-PidFile -PidFile $PidFile
        return $null
    }
}

function Get-PortOwnerPid {
    param([int]$Port)

    $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $connection) {
        return $null
    }

    return [int]$connection.OwningProcess
}

function Get-ProcessCommandLine {
    param([int]$ProcessId)

    $processInfo = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction SilentlyContinue
    if ($null -eq $processInfo) {
        return ""
    }

    return [string]$processInfo.CommandLine
}

function Stop-ProcessTree {
    param([int]$ProcessId)

    & taskkill /PID $ProcessId /T /F | Out-Null
}

function Start-BackgroundShellCommand {
    param(
        [string]$WorkingDirectory,
        [string]$Command,
        [string]$LogFile,
        [string]$PidFile
    )

    if (Test-Path -LiteralPath $LogFile) {
        Remove-Item -LiteralPath $LogFile -Force -ErrorAction SilentlyContinue
    }

    $cmdLine = "/c $Command > `"$LogFile`" 2>&1"
    $process = Start-Process -FilePath "cmd.exe" -ArgumentList $cmdLine -WorkingDirectory $WorkingDirectory -WindowStyle Hidden -PassThru
    Save-Pid -PidFile $PidFile -ProcessId $process.Id
    return $process
}

function Wait-HttpReady {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Url -TimeoutSec 3
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                return $true
            }
        } catch {
        }

        Start-Sleep -Seconds 2
    }

    return $false
}

function Wait-PortClosed {
    param(
        [int]$Port,
        [int]$TimeoutSeconds = 30
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (-not (Get-PortOwnerPid -Port $Port)) {
            return $true
        }
        Start-Sleep -Seconds 1
    }

    return $false
}

function Test-DockerReady {
    try {
        docker version *> $null
        return ($LASTEXITCODE -eq 0)
    } catch {
        return $false
    }
}

function Wait-DockerReady {
    param([int]$TimeoutSeconds = 180)

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-DockerReady) {
            return $true
        }
        Start-Sleep -Seconds 2
    }

    return $false
}

function Ensure-DockerDesktop {
    if (Test-DockerReady) {
        return
    }

    $dockerDesktop = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    if (-not (Test-Path -LiteralPath $dockerDesktop)) {
        throw "Docker Desktop was not found at $dockerDesktop"
    }

    Write-Info "Starting Docker Desktop..."
    Start-Process -FilePath $dockerDesktop | Out-Null

    if (-not (Wait-DockerReady -TimeoutSeconds 180)) {
        throw "Docker Desktop did not become ready in time."
    }
}

function Test-DockerContainerExists {
    param([string]$Name)

    try {
        docker inspect $Name *> $null
        return ($LASTEXITCODE -eq 0)
    } catch {
        return $false
    }
}

function Get-DockerContainerStatus {
    param([string]$Name)

    if (-not (Test-DockerContainerExists -Name $Name)) {
        return $null
    }

    return (docker inspect --format '{{.State.Status}}' $Name 2>$null | Select-Object -First 1)
}

function Test-DockerVolumeExists {
    param([string]$Name)

    try {
        docker volume inspect $Name *> $null
        return ($LASTEXITCODE -eq 0)
    } catch {
        return $false
    }
}

function Wait-MySqlReady {
    param(
        [string]$ContainerName,
        [string]$Password = "root",
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $probe = Start-Process `
                -FilePath "cmd.exe" `
                -ArgumentList "/c docker exec $ContainerName mysqladmin ping -p$Password --silent 1>nul 2>nul" `
                -WindowStyle Hidden `
                -PassThru `
                -Wait
            if ($probe.ExitCode -eq 0) {
                return $true
            }
        } catch {
        }

        Start-Sleep -Seconds 2
    }

    return $false
}

function Show-LogTail {
    param(
        [string]$LogFile,
        [int]$Lines = 60
    )

    if (Test-Path -LiteralPath $LogFile) {
        Get-Content -LiteralPath $LogFile -Tail $Lines
    }
}
