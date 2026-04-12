#!/usr/bin/env bash
# 生产构建并同步到远程服务器（需本机已安装 rsync、ssh，Linux/macOS/Git Bash 均可）
#
# 用法（在 frontend 目录）:
#   chmod +x scripts/deploy.sh
#   ./scripts/deploy.sh
#
# 覆盖默认目标（可选）:
#   DEPLOY_SSH_USER=root DEPLOY_SSH_HOST=114.55.166.12 DEPLOY_REMOTE_PATH=/usr/share/nginx/ztbgl ./scripts/deploy.sh
#   DEPLOY_SSH_PORT=22

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

REMOTE_USER="${DEPLOY_SSH_USER:-root}"
REMOTE_HOST="${DEPLOY_SSH_HOST:-114.55.166.12}"
REMOTE_PATH="${DEPLOY_REMOTE_PATH:-/usr/share/nginx/ztbgl}"
SSH_PORT="${DEPLOY_SSH_PORT:-22}"

echo "==> 安装依赖（如需跳过请先执行过 npm ci / npm install）"
if [[ ! -d node_modules ]]; then
  npm ci 2>/dev/null || npm install
fi

echo "==> 生产构建 npm run build:prod"
npm run build:prod

if [[ ! -d dist ]]; then
  echo "错误: 未生成 dist 目录" >&2
  exit 1
fi

echo "==> 同步 dist/ -> ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/"
echo "    （rsync --delete 会删除远端目录中本地已不存在的文件）"
ssh -p "${SSH_PORT}" -o StrictHostKeyChecking=accept-new "${REMOTE_USER}@${REMOTE_HOST}" "mkdir -p '${REMOTE_PATH}'"
rsync -avz --delete -e "ssh -p ${SSH_PORT} -o StrictHostKeyChecking=accept-new" \
  dist/ "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/"

echo "==> 完成。请确认服务器 Nginx/站点 root 指向: ${REMOTE_PATH}"
