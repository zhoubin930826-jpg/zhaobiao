# Build production bundle and upload via scp (requires OpenSSH client on Windows).
#
# Usage (from frontend directory):
#   powershell -ExecutionPolicy Bypass -File scripts/deploy.ps1
#
# Example with params:
#   .\scripts\deploy.ps1 -RemoteHost "114.55.166.12" -RemoteUser "root" -RemotePath "/usr/share/nginx/ztbgl" -SshPort 22

param(
    [string] $RemoteHost = "114.55.166.12",
    [string] $RemoteUser = "root",
    [string] $RemotePath = "/usr/share/nginx/ztbgl",
    [int] $SshPort = 22
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

if (-not (Test-Path "node_modules")) {
    Write-Host "==> npm install"
    npm ci 2>$null; if (-not $?) { npm install }
}

Write-Host "==> npm run build:prod"
npm run build:prod
if (-not (Test-Path "dist")) {
    throw "Build failed: dist directory missing."
}

$sshTarget = ('{0}@{1}' -f $RemoteUser, $RemoteHost)
Write-Host "==> mkdir remote: $RemotePath"
ssh -p $SshPort -o StrictHostKeyChecking=accept-new $sshTarget "mkdir -p '$RemotePath'"

Write-Host "==> scp dist/* -> ${sshTarget}:$RemotePath/"
ssh -p $SshPort $sshTarget "rm -rf '$RemotePath'/*"
scp -P $SshPort -r "$Root/dist/*" "${sshTarget}:$RemotePath/"

Write-Host "==> Done. Point nginx root to: $RemotePath"
