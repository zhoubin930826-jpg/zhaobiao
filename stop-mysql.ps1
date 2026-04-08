. "$PSScriptRoot\_script-common.ps1"

$containerName = "zhaobiao-mysql"

if (-not (Test-DockerReady)) {
    Write-WarnMessage "Docker Desktop is not running. MySQL container is already unavailable."
    exit 0
}

$status = Get-DockerContainerStatus -Name $containerName
if (-not $status) {
    Write-WarnMessage "MySQL container does not exist."
    exit 0
}

if ($status -ne "running") {
    Write-WarnMessage "MySQL container is not running."
    exit 0
}

Write-Info "Stopping MySQL container..."
docker stop $containerName | Out-Null
Write-Ok "MySQL container stopped."
