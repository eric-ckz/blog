#!/usr/bin/env bash
set -Eeuo pipefail

# 使用 Docker 中的 Certbot 完成首次签发与后续续签，不在宿主机安装额外的 Certbot 软件包。
if [[ "${EUID}" -ne 0 ]]; then
  echo "请使用 sudo bash deploy/scripts/enable_https.sh 执行" >&2
  exit 1
fi

DOMAIN="${BLOG_DOMAIN:-blog.45205044.xyz}"
EMAIL="${LETSENCRYPT_EMAIL:-}"
RELEASE_ROOT="${BLOG_RELEASE_ROOT:-/opt/charles-blog/current}"
COMPOSE_FILE="${RELEASE_ROOT}/deploy/docker-compose.yml"
CERTBOT_WEBROOT="/var/lib/charles-blog/certbot-www"

if [[ ! -r "${COMPOSE_FILE}" ]]; then
  echo "未找到部署目录：${RELEASE_ROOT}" >&2
  exit 1
fi

install -d -m 0755 "${CERTBOT_WEBROOT}/.well-known/acme-challenge"
install -d -m 0755 /etc/letsencrypt

# 如果此前为 Cloudflare Full 模式创建过临时自签名 Origin 证书，先仅删除该明确标记的
# 临时目录，避免其与 Certbot 的正式证书 lineage 冲突。不会删除其他域名的任何证书。
TEMPORARY_CERT_MARKER="/etc/letsencrypt/live/${DOMAIN}/.charles-blog-self-signed"
if [[ -f "${TEMPORARY_CERT_MARKER}" ]]; then
  rm -rf "/etc/letsencrypt/live/${DOMAIN}" \
         "/etc/letsencrypt/archive/${DOMAIN}" \
         "/etc/letsencrypt/renewal/${DOMAIN}.conf"
fi

# 先以不依赖证书的 HTTP 配置启动，保证 Cloudflare 仍可将 HTTP-01 请求回源到宿主机。
export NGINX_CONFIG_FILE=nginx.http.conf
docker compose -p charles-blog -f "${COMPOSE_FILE}" up -d --force-recreate

CERTBOT_ARGS=(certonly --webroot -w /var/www/certbot --agree-tos --no-eff-email --keep-until-expiring -d "${DOMAIN}")
if [[ -n "${EMAIL}" ]]; then
  CERTBOT_ARGS+=(--email "${EMAIL}")
else
  # 未提供邮箱时仍可签发；自动续签 timer 可避免证书因忘记续期而中断。
  CERTBOT_ARGS+=(--register-unsafely-without-email)
fi

docker run --rm \
  -v /etc/letsencrypt:/etc/letsencrypt \
  -v "${CERTBOT_WEBROOT}:/var/www/certbot" \
  certbot/certbot:v2.11.0 "${CERTBOT_ARGS[@]}"

# 证书存在后切换到 TLS 配置，HTTP 仅保留校验入口并永久跳转到 HTTPS。
unset NGINX_CONFIG_FILE
docker compose -p charles-blog -f "${COMPOSE_FILE}" up -d --force-recreate

# Refresh Token Cookie 仅应在 HTTPS 下发送，防止明文连接携带认证凭证。
sed -i 's/^BLOG_SECURE_COOKIE=.*/BLOG_SECURE_COOKIE=true/' /etc/charles-blog/blog-server.env
systemctl restart blog-server.service

install -m 0644 "${RELEASE_ROOT}/deploy/systemd/charles-blog-certbot.service" \
  /etc/systemd/system/charles-blog-certbot.service
install -m 0644 "${RELEASE_ROOT}/deploy/systemd/charles-blog-certbot.timer" \
  /etc/systemd/system/charles-blog-certbot.timer
systemctl daemon-reload
systemctl enable --now charles-blog-certbot.timer

curl --fail --silent --show-error --retry 10 --retry-delay 2 \
  "https://${DOMAIN}/api/site/home" >/dev/null
echo "HTTPS_ENABLED domain=${DOMAIN}"
