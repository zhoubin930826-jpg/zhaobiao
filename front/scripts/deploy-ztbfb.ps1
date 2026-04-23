# 门户 front：生产构建并上传到服务器（需本机 OpenSSH：ssh、scp）。
# 与 frontend/scripts/deploy.ps1 一致，默认远端目录为 ztbfb。
#
# 仅本地构建（不 SSH）:
#   powershell -ExecutionPolicy Bypass -File scripts/deploy-ztbfb.ps1 -LocalOnly
# 或: npm run pack:ztbfb
#
# 构建并上传:
#   powershell -ExecutionPolicy Bypass -File scripts/deploy-ztbfb.ps1
# 或: npm run deploy:ztbfb

param(
    [switch] $LocalOnly,
    [string] $RemoteHost = "114.55.166.12",
    [string] $RemoteUser = "root",
    [string] $RemotePath = "/usr/share/nginx/ztbfb",
    [int] $SshPort = 22
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

if (-not (Test-Path "node_modules")) {
    Write-Host "==> npm install"
    npm ci 2>$null; if (-not $?) { npm install }
}

Write-Host "==> npm run build"
npm run build
if (-not (Test-Path "dist")) {
    throw "Build failed: dist directory missing."
}

if ($LocalOnly) {
    Write-Host "==> LocalOnly: skip upload. Output: $Root\dist"
    exit 0
}

$sshTarget = ('{0}@{1}' -f $RemoteUser, $RemoteHost)
Write-Host "==> mkdir remote: $RemotePath"
ssh -p $SshPort -o StrictHostKeyChecking=accept-new $sshTarget "mkdir -p '$RemotePath'"

Write-Host "==> scp dist/* -> ${sshTarget}:$RemotePath/"
ssh -p $SshPort $sshTarget "rm -rf '$RemotePath'/*"
scp -P $SshPort -r "$Root/dist/*" "${sshTarget}:$RemotePath/"

Write-Host "==> Done. Point nginx root to: $RemotePath"
