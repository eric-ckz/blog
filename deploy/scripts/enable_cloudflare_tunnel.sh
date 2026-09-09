#!/usr/bin/env bash
set -Eeuo pipefail

# Cloudflare Tunnel 让源站主动连接边缘网络。浏览器看到的公开证书由 Cloudflare 自动续期，
# 因此不再依赖腾讯云公网 80/443、HTTP-01 校验或源站自签名证书。
if [[ "${EUID}" -ne 0 ]]; then
  echo "请使用 sudo 执行该脚本" >&2
  exit 1
fi

TOKEN="${CLOUDFLARE_TUNNEL_TOKEN:-}"
RELEASE_ROOT="${BLOG_RELEASE_ROOT:-/opt/eric-blog/current}"
COMPOSE_FILE="${RELEASE_ROOT}/deploy/docker-compose.yml"

if [[ -z "${TOKEN}" ]]; then
  echo "缺少 CLOUDFLARE_TUNNEL_TOKEN" >&2
  exit 1
fi

if [[ ! -r "${COMPOSE_FILE}" ]]; then
  echo "未找到 Compose 配置：${COMPOSE_FILE}" >&2
  exit 1
fi

# Token 只落盘到 root-only 环境文件，不写入脚本、发布目录或命令参数。
install -d -m 0700 /etc/eric-blog
umask 077
printf 'TUNNEL_TOKEN=%s\n' "${TOKEN}" > /etc/eric-blog/cloudflared.env
unset TOKEN CLOUDFLARE_TUNNEL_TOKEN

docker compose --profile tunnel -p eric-blog -f "${COMPOSE_FILE}" pull cloudflared
docker compose --profile tunnel -p eric-blog -f "${COMPOSE_FILE}" up -d cloudflared

for _ in {1..30}; do
  if docker logs eric-blog-cloudflared 2>&1 | grep -q 'Registered tunnel connection'; then
    echo "CLOUDFLARE_TUNNEL_CONNECTED"
    exit 0
  fi
  sleep 2
done

docker logs --tail 80 eric-blog-cloudflared >&2
echo "Tunnel 未在预期时间内建立连接" >&2
exit 1
