#!/usr/bin/env bash
# 构建并 rsync 到服务器 ztbfb 目录（与 frontend/scripts/deploy.sh 一致，路径改为 ztbfb）
#
# 用法（在 front 目录）:
#   bash scripts/deploy-ztbfb.sh
#
# 环境变量覆盖:
#   DEPLOY_SSH_USER=root DEPLOY_SSH_HOST=114.55.166.12 DEPLOY_REMOTE_PATH=/usr/share/nginx/ztbfb ./scripts/deploy-ztbfb.sh

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

REMOTE_USER="${DEPLOY_SSH_USER:-root}"
REMOTE_HOST="${DEPLOY_SSH_HOST:-114.55.166.12}"
REMOTE_PATH="${DEPLOY_REMOTE_PATH:-/usr/share/nginx/ztbfb}"
SSH_PORT="${DEPLOY_SSH_PORT:-22}"

echo "==> npm install (if needed)"
if [[ ! -d node_modules ]]; then
  npm ci 2>/dev/null || npm install
fi

echo "==> npm run build"
npm run build

if [[ ! -d dist ]]; then
  echo "错误: 未生成 dist 目录" >&2
  exit 1
fi

echo "==> mkdir + rsync dist/ -> ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/"
ssh -p "${SSH_PORT}" -o StrictHostKeyChecking=accept-new "${REMOTE_USER}@${REMOTE_HOST}" "mkdir -p '${REMOTE_PATH}'"
rsync -avz --delete -e "ssh -p ${SSH_PORT} -o StrictHostKeyChecking=accept-new" \
  dist/ "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_PATH}/"

echo "==> Done. Nginx root: ${REMOTE_PATH}"
