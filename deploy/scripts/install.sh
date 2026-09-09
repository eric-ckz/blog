#!/usr/bin/env bash
set -Eeuo pipefail

# 该脚本由 sudo bash 执行。部署包本身不携带密码，敏感值从 root-only 文件读取。
if [[ "${EUID}" -ne 0 ]]; then
  echo "请使用 sudo bash deploy/scripts/install.sh 执行" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGE_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEPLOY_ENV="${DEPLOY_ENV:-/etc/eric-blog/install.env}"

if [[ ! -r "${DEPLOY_ENV}" ]]; then
  echo "缺少仅限 root 读取的部署变量文件：${DEPLOY_ENV}" >&2
  exit 1
fi

# shellcheck disable=SC1090
source "${DEPLOY_ENV}"
: "${MYSQL_ROOT_PASSWORD:?缺少 MYSQL_ROOT_PASSWORD}"
: "${BLOG_JWT_SECRET:?缺少 BLOG_JWT_SECRET}"
: "${BLOG_ADMIN_PASSWORD:?缺少 BLOG_ADMIN_PASSWORD}"

VERSION="$(tr -d '\r\n' < "${PACKAGE_ROOT}/VERSION")"
if [[ ! "${VERSION}" =~ ^[0-9]{8}-[0-9]{6}$ ]]; then
  echo "非法部署版本：${VERSION}" >&2
  exit 1
fi

if [[ -f "${PACKAGE_ROOT}/manifest.sha256" ]]; then
  (cd "${PACKAGE_ROOT}" && sha256sum --check manifest.sha256)
fi

BACKUP_ROOT="/home/ubuntu/backups/pre-blog-${VERSION}"
RELEASE_ROOT="/opt/eric-blog/releases/${VERSION}"

backup_existing_services() {
  install -d -m 0700 "${BACKUP_ROOT}"

  # Halo 备份包含可执行文件、数据目录、环境变量和 systemd unit，确保可以完整恢复。
  tar -C / -czf "${BACKUP_ROOT}/halo.tar.gz" \
    opt/halo var/lib/halo2 etc/systemd/system/halo.service etc/halo.env

  install -d -m 0700 "${BACKUP_ROOT}/sub2api"
  cp -a /home/sub2api/docker-compose.yml "${BACKUP_ROOT}/sub2api/"
  if [[ -f /home/sub2api/.env ]]; then
    cp -a /home/sub2api/.env "${BACKUP_ROOT}/sub2api/"
  fi
  docker inspect sub2api sub2api-postgres sub2api-redis \
    > "${BACKUP_ROOT}/sub2api/docker-inspect.json"
  (cd /home/sub2api && docker compose ps --all) \
    > "${BACKUP_ROOT}/sub2api/compose-ps.txt"
  chmod -R go-rwx "${BACKUP_ROOT}"
}

stop_existing_services() {
  systemctl disable --now halo.service
  (cd /home/sub2api && docker compose stop --timeout 60)
}

install_mysql() {
  export DEBIAN_FRONTEND=noninteractive
  apt-get update
  apt-get install -y mysql-server curl ca-certificates unzip
  install -m 0644 "${PACKAGE_ROOT}/deploy/mysql/90-eric-blog.cnf" \
    /etc/mysql/mysql.conf.d/90-eric-blog.cnf
  systemctl enable --now mysql.service
  systemctl restart mysql.service

  local client_file
  client_file="$(mktemp)"
  chmod 0600 "${client_file}"
  cat > "${client_file}" <<EOF
[client]
user=root
password=${MYSQL_ROOT_PASSWORD}
host=127.0.0.1
protocol=tcp
EOF

  if mysql --protocol=socket -uroot -Nse "SELECT 1" >/dev/null 2>&1; then
    mysql --protocol=socket -uroot <<SQL
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_ROOT_PASSWORD}';
CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_ROOT_PASSWORD}';
ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_ROOT_PASSWORD}';
GRANT ALL PRIVILEGES ON blog.* TO 'root'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
  else
    mysql --defaults-extra-file="${client_file}" <<SQL
CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_ROOT_PASSWORD}';
CREATE USER IF NOT EXISTS 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_ROOT_PASSWORD}';
ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH caching_sha2_password BY '${MYSQL_ROOT_PASSWORD}';
GRANT ALL PRIVILEGES ON blog.* TO 'root'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
  fi

  mysql --defaults-extra-file="${client_file}" -Nse \
    "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='blog'"
  rm -f "${client_file}"
}

install_release() {
  install -d -m 0755 /opt/eric-blog/releases
  if [[ -e "${RELEASE_ROOT}" ]]; then
    echo "版本目录已存在，拒绝覆盖：${RELEASE_ROOT}" >&2
    exit 1
  fi

  install -d -m 0755 "${RELEASE_ROOT}"
  cp -a "${PACKAGE_ROOT}/web" "${RELEASE_ROOT}/web"
  cp -a "${PACKAGE_ROOT}/admin" "${RELEASE_ROOT}/admin"
  cp -a "${PACKAGE_ROOT}/server" "${RELEASE_ROOT}/server"
  cp -a "${PACKAGE_ROOT}/deploy" "${RELEASE_ROOT}/deploy"
  chown -R root:root "${RELEASE_ROOT}"
  chmod -R a-w "${RELEASE_ROOT}"

  install -d -o ubuntu -g ubuntu -m 0750 /var/lib/eric-blog/uploads
  install -d -o ubuntu -g ubuntu -m 0750 /var/log/eric-blog
  # 种子数据中的 URL 固定包含 2026/07/seed，文件必须保持相同层级才能被 /uploads 正确访问。
  install -d -o ubuntu -g ubuntu -m 0750 /var/lib/eric-blog/uploads/2026/07/seed
  cp -an "${PACKAGE_ROOT}/seed-uploads/." /var/lib/eric-blog/uploads/2026/07/seed/
  chown -R ubuntu:ubuntu /var/lib/eric-blog/uploads /var/log/eric-blog

  ln -sfn "releases/${VERSION}" /opt/eric-blog/current.next
  mv -Tf /opt/eric-blog/current.next /opt/eric-blog/current
}

install_runtime_config() {
  install -d -m 0700 /etc/eric-blog
  cat > /etc/eric-blog/blog-server.env <<EOF
SPRING_PROFILES_ACTIVE=prod
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=8080
BLOG_DB_URL=jdbc:mysql://127.0.0.1:3306/blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
BLOG_DB_USERNAME=root
BLOG_DB_PASSWORD=${MYSQL_ROOT_PASSWORD}
BLOG_JWT_SECRET=${BLOG_JWT_SECRET}
BLOG_STORAGE_ROOT=/var/lib/eric-blog/uploads
BLOG_LOG_FILE=/var/log/eric-blog/blog-server.log
# Cloudflare 可能以 HTTPS 向浏览器提供站点，但回源仍使用 HTTP，因此两种浏览器 Origin 都必须精确允许。
BLOG_CORS_ALLOWED_ORIGINS=http://blog.45205044.xyz,https://blog.45205044.xyz
BLOG_SECURE_COOKIE=false
BLOG_ADMIN_USERNAME=admin
BLOG_ADMIN_PASSWORD=${BLOG_ADMIN_PASSWORD}
BLOG_ADMIN_DISPLAY_NAME=博客管理员
EOF
  chmod 0600 /etc/eric-blog/blog-server.env

  install -m 0644 "${PACKAGE_ROOT}/deploy/systemd/blog-server.service" \
    /etc/systemd/system/blog-server.service
  install -m 0644 "${PACKAGE_ROOT}/deploy/logrotate/eric-blog" \
    /etc/logrotate.d/eric-blog
  systemctl daemon-reload
}

start_and_verify() {
  systemctl enable --now blog-server.service
  for _ in {1..60}; do
    if curl --fail --silent http://127.0.0.1:8080/api/site/home >/dev/null; then
      break
    fi
    sleep 2
  done
  curl --fail --silent http://127.0.0.1:8080/api/site/home >/dev/null

  # 初始管理员成功写入数据库后立即移除环境文件中的明文初始化密码。
  sed -i '/^BLOG_ADMIN_/d' /etc/eric-blog/blog-server.env
  systemctl restart blog-server.service
  curl --retry 30 --retry-delay 2 --retry-connrefused --fail --silent \
    http://127.0.0.1:8080/api/site/home >/dev/null

  docker compose -p eric-blog -f /opt/eric-blog/current/deploy/docker-compose.yml pull
  docker compose -p eric-blog -f /opt/eric-blog/current/deploy/docker-compose.yml up -d --force-recreate
  curl --retry 30 --retry-delay 2 --retry-connrefused --fail --silent \
    http://127.0.0.1/ >/dev/null
  curl --fail --silent http://127.0.0.1/admin/ >/dev/null
  curl --fail --silent http://127.0.0.1/api/site/home >/dev/null
}

prune_old_releases() {
  mapfile -t releases < <(find /opt/eric-blog/releases -mindepth 1 -maxdepth 1 -type d -printf '%f\n' | sort -r)
  if (( ${#releases[@]} > 3 )); then
    for old_release in "${releases[@]:3}"; do
      rm -rf -- "/opt/eric-blog/releases/${old_release}"
    done
  fi
}

backup_existing_services
stop_existing_services
install_mysql
install_release
install_runtime_config
start_and_verify
prune_old_releases

# install.env 只用于本次引导，成功后删除，避免长期保留管理员初始明文密码。
rm -f "${DEPLOY_ENV}"

echo "部署完成：${VERSION}"
