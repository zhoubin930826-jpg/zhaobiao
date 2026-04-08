. "$PSScriptRoot\_script-common.ps1"

$containerName = "zhaobiao-mysql"
$volumeName = "zhaobiao-mysql-data"
$password = "root"
$database = "zhaobiao_admin"

Ensure-DockerDesktop

$status = Get-DockerContainerStatus -Name $containerName
if ($status -eq "running") {
    Write-WarnMessage "MySQL container is already running."
} elseif ($status) {
    Write-Info "Starting existing MySQL container..."
    docker start $containerName | Out-Null
} else {
    if (-not (Test-DockerVolumeExists -Name $volumeName)) {
        Write-Info "Creating Docker volume $volumeName..."
        docker volume create $volumeName | Out-Null
    }

    Write-Info "Creating MySQL container..."
    docker run -d `
        --name $containerName `
        -p 3306:3306 `
        -e "MYSQL_ROOT_PASSWORD=$password" `
        -e "MYSQL_DATABASE=$database" `
        -v "${volumeName}:/var/lib/mysql" `
        mysql:8.0 `
        --character-set-server=utf8mb4 `
        --collation-server=utf8mb4_general_ci | Out-Null
}

if (-not (Wait-MySqlReady -ContainerName $containerName -Password $password -TimeoutSeconds 120)) {
    docker logs --tail 80 $containerName
    throw "MySQL did not become ready in time."
}

Write-Ok "MySQL is ready."
Write-Host "Host: localhost"
Write-Host "Port: 3306"
Write-Host "Database: $database"
Write-Host "User: root"
Write-Host "Password: $password"

