# Charles Blog 从构建到生产部署完整手册

本文记录当前项目从本地构建三个应用，到 Ubuntu 服务器、MySQL、systemd、Docker Nginx、
Cloudflare Tunnel 和公网 HTTPS 的完整部署流程。命令以当前项目和域名为例，但所有密码、
JWT 密钥及 Tunnel Token 都必须在执行时单独生成，不能提交到 Git。

## 1. 最终架构

```text
浏览器
  |
  | HTTPS: https://blog.45205044.xyz
  v
Cloudflare 边缘证书 + HTTP 强制跳转
  |
  | Cloudflare Tunnel 加密出站连接
  v
cloudflared Docker 容器
  |
  | HTTP: 127.0.0.1:80（仅服务器内部回源）
  v
Nginx Docker 容器（host 网络）
  |-- /             -> blog-web 静态文件
  |-- /admin/       -> blog-admin 静态文件
  |-- /api/         -> 127.0.0.1:8080
  `-- /uploads/     -> 127.0.0.1:8080
                          |
                          v
                  blog-server systemd 服务
                          |
                          v
                  MySQL 127.0.0.1:3306
```

最终访问地址：

- 用户端：`https://blog.45205044.xyz/`
- 管理端：`https://blog.45205044.xyz/admin/`
- 公开 API 示例：`https://blog.45205044.xyz/api/site/home`
- Java、MySQL 不对公网开放。
- 用户端和管理端不是两个常驻 Node 服务，生产环境只部署各自的 `dist` 静态文件。

## 2. 目录约定

本地项目：

```text
blog-web/       Vue 3 用户端
blog-admin/     Vue 3 + Element Plus 管理端
blog-server/    Java 21 + Spring Boot API
deploy/         生产部署配置和脚本
artifacts/      本地生成的部署 ZIP，不提交 Git
```

服务器：

```text
/opt/charles-blog/releases/<版本号>/   不可变发布版本
/opt/charles-blog/current              当前版本软链接
/etc/charles-blog/                     root-only 敏感配置
/var/lib/charles-blog/uploads/         永久上传文件
/var/log/charles-blog/                 Java 日志
/home/ubuntu/                           上传和解压暂存区
```

发布版本目录包含：

```text
web/                 blog-web/dist
admin/               blog-admin/dist
server/blog-server.jar
deploy/              Nginx、Compose、systemd 和脚本
```

数据库、上传文件和日志不放在版本目录中，因此切换或删除旧版本不会丢失业务数据。

## 3. 本地构建环境

需要安装：

- Node.js 20 或更高版本
- npm
- Java 21
- Maven 3.9 或更高版本
- Python 3.11 或更高版本

先确认生产环境变量：

`blog-web/.env.production`：

```env
VITE_DATA_MODE=api
VITE_API_BASE_URL=/api
```

`blog-admin/.env.production`：

```env
VITE_API_BASE_URL=/api
VITE_BLOG_WEB_URL=https://blog.45205044.xyz/
```

两个前端都使用同源相对地址 `/api`，不要在生产构建中写 `localhost:8080` 或服务器 IP。
图片使用 `/uploads`，同样由 Nginx 代理。

## 4. 构建三个项目

在项目根目录执行以下 PowerShell 命令。

### 4.1 用户端

```powershell
Set-Location .\blog-web
npm ci
npm test
npm run build
Set-Location ..
```

输出目录为 `blog-web/dist`。

### 4.2 管理端

```powershell
Set-Location .\blog-admin
npm ci
npm test
npm run build
Set-Location ..
```

输出目录为 `blog-admin/dist`。管理端生产构建的 Vite `base` 是 `/admin/`，不能把该目录
直接挂载到域名根路径。

### 4.3 Java 后端

```powershell
Set-Location .\blog-server
mvn clean test package
Set-Location ..
```

输出文件为：

```text
blog-server/target/blog-server-1.0.0.jar
```

H2 只参与自动化测试；开发和生产运行均使用 MySQL。Flyway SQL 已打入 JAR，Java 第一次连接
空的 `blog` 数据库时会自动建表、创建索引并导入初始内容。

## 5. 生成部署包

打包脚本要求以下内容已经存在：

- `blog-web/dist`
- `blog-admin/dist`
- `blog-server/target/blog-server-1.0.0.jar`
- `blog-server/uploads/2026/07/seed` 中的种子图片

执行：

```powershell
python .\deploy\scripts\package.py
```

也可以指定版本号：

```powershell
python .\deploy\scripts\package.py --version 20260801-120000
```

输出示例：

```text
VERSION=20260801-120000
ZIP=D:\java-project\myself\blog\artifacts\blog-deploy-20260801-120000.zip
SIZE=...
SHA256=...
```

ZIP 只包含构建产物、种子图片和部署配置，不包含源码、`node_modules`、本地数据库、
`.env`、数据库密码、JWT 密钥或 SSH 密码。`manifest.sha256` 用于服务器解压后的逐文件校验。

## 6. 上传服务器

推荐上传到：

```text
/home/ubuntu/blog-deploy.zip
```

可以使用图形化 SFTP 工具，也可以在本机执行：

```powershell
scp .\artifacts\blog-deploy-<版本号>.zip ubuntu@<服务器IP>:/home/ubuntu/blog-deploy.zip
```

上传完成后登录服务器：

```bash
ssh ubuntu@<服务器IP>
```

先核对 ZIP 摘要与本地打包输出一致：

```bash
sha256sum /home/ubuntu/blog-deploy.zip
```

然后解压到独立暂存目录：

```bash
VERSION="$(unzip -p /home/ubuntu/blog-deploy.zip VERSION | tr -d '\r\n')"
STAGING="/home/ubuntu/blog-deploy-${VERSION}"
mkdir -p "${STAGING}"
unzip /home/ubuntu/blog-deploy.zip -d "${STAGING}"
cd "${STAGING}"
sha256sum --check manifest.sha256
```

只有所有文件都显示 `OK` 才继续部署。

## 7. 服务器基础环境

当前目标系统是 Ubuntu 24.04。安装 Java 21、Docker、Compose、MySQL 及常用工具：

```bash
sudo apt-get update
sudo apt-get install -y \
  openjdk-21-jre-headless \
  docker.io docker-compose-v2 \
  mysql-server curl ca-certificates unzip

sudo systemctl enable --now docker mysql
java -version
docker version
docker compose version
mysql --version
```

Java 服务按当前约定使用 `ubuntu` 用户运行，不创建单独的 `blog` 用户。

如果服务器已有 Halo 或 sub2api，停止前先备份并记录状态：

```bash
sudo systemctl status halo --no-pager
cd /home/sub2api && sudo docker compose ps --all

sudo systemctl disable --now halo
cd /home/sub2api && sudo docker compose stop
```

使用 `stop` 而不是 `down`，不要删除容器、镜像、卷或数据库。纯净服务器可以跳过这一段。

## 8. 配置 MySQL

安装项目提供的资源限制配置：

```bash
sudo install -m 0644 \
  "${STAGING}/deploy/mysql/90-charles-blog.cnf" \
  /etc/mysql/mysql.conf.d/90-charles-blog.cnf
sudo systemctl restart mysql
```

该配置会：

- 只监听 `127.0.0.1:3306`
- 使用 `utf8mb4`
- 将 InnoDB 缓冲池限制为 256MB
- 将最大连接数限制为 50

为 MySQL root 设置强密码，并创建数据库。以下命令中的占位符必须替换，密码不要写进 Git：

```bash
sudo mysql
```

进入 MySQL 后执行：

```sql
ALTER USER 'root'@'localhost'
  IDENTIFIED WITH caching_sha2_password BY '<MYSQL_ROOT_PASSWORD>';

CREATE DATABASE IF NOT EXISTS blog
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'root'@'127.0.0.1'
  IDENTIFIED WITH caching_sha2_password BY '<MYSQL_ROOT_PASSWORD>';

ALTER USER 'root'@'127.0.0.1'
  IDENTIFIED WITH caching_sha2_password BY '<MYSQL_ROOT_PASSWORD>';

GRANT ALL PRIVILEGES ON blog.* TO 'root'@'127.0.0.1';
FLUSH PRIVILEGES;
EXIT;
```

当前项目按约定使用 root。更严格的生产环境建议改成只能访问 `blog.*` 的独立数据库用户。

确认监听地址和数据库：

```bash
sudo ss -lntp | grep 3306
mysql -h 127.0.0.1 -uroot -p -e "SHOW DATABASES LIKE 'blog';"
```

## 9. 安装发布文件

创建不可变版本目录和持久化目录：

```bash
RELEASE="/opt/charles-blog/releases/${VERSION}"

sudo install -d -m 0755 /opt/charles-blog/releases
sudo install -d -m 0755 "${RELEASE}"
sudo cp -a "${STAGING}/web" "${RELEASE}/web"
sudo cp -a "${STAGING}/admin" "${RELEASE}/admin"
sudo cp -a "${STAGING}/server" "${RELEASE}/server"
sudo cp -a "${STAGING}/deploy" "${RELEASE}/deploy"

sudo chown -R root:root "${RELEASE}"
sudo chmod -R a-w "${RELEASE}"

sudo install -d -o ubuntu -g ubuntu -m 0750 \
  /var/lib/charles-blog/uploads \
  /var/log/charles-blog

sudo install -d -o ubuntu -g ubuntu -m 0750 \
  /var/lib/charles-blog/uploads/2026/07/seed
sudo cp -an "${STAGING}/seed-uploads/." \
  /var/lib/charles-blog/uploads/2026/07/seed/
sudo chown -R ubuntu:ubuntu \
  /var/lib/charles-blog/uploads \
  /var/log/charles-blog

sudo ln -sfn "releases/${VERSION}" /opt/charles-blog/current.next
sudo mv -Tf /opt/charles-blog/current.next /opt/charles-blog/current
```

不要把上传目录放到 `/opt/charles-blog/current` 中，否则版本回滚会覆盖或丢失用户上传文件。

## 10. 配置 Java 生产环境

生成 JWT 密钥：

```bash
openssl rand -base64 48
```

创建仅 root 可读的环境文件：

```bash
sudo install -d -m 0700 /etc/charles-blog
sudoedit /etc/charles-blog/blog-server.env
```

内容如下：

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=8080
BLOG_DB_URL=jdbc:mysql://127.0.0.1:3306/blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
BLOG_DB_USERNAME=root
BLOG_DB_PASSWORD=<MYSQL_ROOT_PASSWORD>
BLOG_JWT_SECRET=<OPENSSL生成的随机密钥>
BLOG_STORAGE_ROOT=/var/lib/charles-blog/uploads
BLOG_LOG_FILE=/var/log/charles-blog/blog-server.log
BLOG_CORS_ALLOWED_ORIGINS=https://blog.45205044.xyz
BLOG_SECURE_COOKIE=true
BLOG_ADMIN_USERNAME=admin
BLOG_ADMIN_PASSWORD=<仅首次启动使用的强密码>
BLOG_ADMIN_DISPLAY_NAME=博客管理员
```

设置权限：

```bash
sudo chown root:root /etc/charles-blog/blog-server.env
sudo chmod 600 /etc/charles-blog/blog-server.env
```

`BLOG_STORAGE_ROOT` 必须使用绝对路径。若使用 `./uploads`，systemd 工作目录会影响最终位置，
容易把文件保存到错误目录。

## 11. 安装并启动 Java systemd 服务

```bash
sudo install -m 0644 \
  /opt/charles-blog/current/deploy/systemd/blog-server.service \
  /etc/systemd/system/blog-server.service

sudo install -m 0644 \
  /opt/charles-blog/current/deploy/logrotate/charles-blog \
  /etc/logrotate.d/charles-blog

sudo systemctl daemon-reload
sudo systemctl enable --now blog-server
sudo systemctl status blog-server --no-pager
```

首次启动时 Flyway 会执行数据库迁移，随后根据 `BLOG_ADMIN_*` 创建初始管理员。确认 API 正常：

```bash
curl --fail http://127.0.0.1:8080/api/site/home
sudo journalctl -u blog-server -n 100 --no-pager
mysql -h 127.0.0.1 -uroot -p blog \
  -e "SELECT installed_rank, version, description, success FROM flyway_schema_history;"
```

管理员创建成功后，从环境文件删除初始化明文密码，保留其他生产变量：

```bash
sudo sed -i '/^BLOG_ADMIN_/d' /etc/charles-blog/blog-server.env
sudo systemctl restart blog-server
```

管理员已保存在数据库中，后续启动不再依赖这些初始化变量。首次登录后还应在管理端修改密码。

## 12. 启动 Docker Nginx

当前公网 HTTPS 由 Cloudflare 管理，Tunnel 在服务器内部回源 `127.0.0.1:80`。首次部署时使用
不依赖源站证书的配置：

```bash
cd /opt/charles-blog/current
sudo NGINX_CONFIG_FILE=nginx.http.conf \
  docker compose -p charles-blog -f deploy/docker-compose.yml \
  up -d nginx
```

验证：

```bash
sudo docker ps --filter name=charles-blog-nginx
sudo docker logs --tail 100 charles-blog-nginx
curl -I http://127.0.0.1/
curl -I http://127.0.0.1/admin/
curl http://127.0.0.1/api/site/home
```

Nginx 路由规则：

- `/`：用户端静态资源，并回退到 `/index.html`
- `/admin/`：管理端静态资源，并回退到 `/admin/index.html`
- `/api/`：反向代理 Java 8080
- `/uploads/`：反向代理 Java 上传资源
- 最大请求体 12MB，与 Java 上传限制匹配

`deploy/nginx/nginx.conf` 中的源站 443 和 Certbot 配置是不用 Tunnel 时的备用方案。当前主方案
不依赖源站证书，也不需要执行 `certbot renew`。

## 13. 配置 Cloudflare DNS

1. 在 Cloudflare 添加 `45205044.xyz`。
2. 到域名注册商或 DNSPod，把域名 NS 修改为 Cloudflare 分配的两个名称服务器。
3. 等待 Cloudflare 显示域名状态为“活动”。
4. 不要让 `blog.45205044.xyz` 同时存在旧 A 和 Tunnel CNAME。

如果原来有：

```text
blog  A  <服务器公网IP>
```

在创建 Tunnel 路由前删除该记录。不要删除 `api`、MX、TXT、DKIM 等其他记录。

## 14. 创建 Cloudflare Tunnel

在 Cloudflare 控制台操作：

1. 进入 `Zero Trust / Networks / Tunnels`。
2. 创建 Cloudflared Tunnel，名称填写 `charles-blog`。
3. 选择 Docker 环境。
4. 复制安装命令中 `--token` 后面的 Token。

Token 不要直接写在 Shell 命令历史中。服务器执行：

```bash
read -rsp 'Tunnel token: ' CLOUDFLARE_TUNNEL_TOKEN; echo
export CLOUDFLARE_TUNNEL_TOKEN
sudo --preserve-env=CLOUDFLARE_TUNNEL_TOKEN \
  bash /opt/charles-blog/current/deploy/scripts/enable_cloudflare_tunnel.sh
unset CLOUDFLARE_TUNNEL_TOKEN
```

脚本会把 Token 写入：

```text
/etc/charles-blog/cloudflared.env
```

文件权限必须是 `600 root:root`。然后确认：

```bash
sudo docker ps --filter name=charles-blog-cloudflared
sudo docker logs --tail 100 charles-blog-cloudflared
sudo stat -c '%a %U:%G %n' /etc/charles-blog/cloudflared.env
```

正常日志中应包含：

```text
Registered tunnel connection
```

## 15. 配置 Tunnel 公网路由

在 Tunnel 的“路由”中添加“已发布的应用程序”：

```text
Hostname: blog.45205044.xyz
Service type: HTTP
URL: 127.0.0.1:80
Path: 留空
```

注意：

- 浏览器到 Cloudflare 使用 HTTPS。
- Cloudflare 到服务器通过 Tunnel 加密连接。
- 只有 cloudflared 到同一台服务器 Nginx 的 loopback 段使用 HTTP。
- 因此 `127.0.0.1:80` 不代表公网使用明文 HTTP。

路由创建后，DNS 最终应为：

```text
blog  CNAME  <tunnel-id>.cfargotunnel.com  已代理
```

如果控制台只创建了路由但仍保留旧 `blog A`，公网请求仍可能到达腾讯云备案拦截页。此时删除
旧 `blog A`，手动创建上述已代理 CNAME。

## 16. 开启公网 HTTPS

在 Cloudflare 操作：

1. 当前部署在 `SSL/TLS / 概述` 使用“灵活”模式：访客到 Cloudflare 是 HTTPS，Tunnel 内部服务
   URL 是 HTTP。Tunnel 本身仍是加密的出站连接。
2. `SSL/TLS / 边缘证书` 中确认 Universal SSL 证书状态为“有效”。
3. 启用“始终使用 HTTPS”。
4. 当前 Tunnel 使用 HTTP loopback 回源，不要在源站 80 再做 HTTPS 跳转，否则可能产生循环。

Cloudflare 的 SSL 模式默认作用于整个域。如果同一域下其他已代理子域必须使用 Full/Strict，
应为 `blog.45205044.xyz` 创建单独的配置规则，或者把 Tunnel 内部服务改成可信 HTTPS，不能在不评估
其他子域的情况下直接修改全局模式。

Cloudflare 会自动轮换公网证书。证书有到期时间是正常现象，不需要在服务器执行 Certbot 续签。

验证 HTTP 跳转及 HTTPS：

```bash
curl -I http://blog.45205044.xyz/
curl -I https://blog.45205044.xyz/
curl -I https://blog.45205044.xyz/admin/
curl https://blog.45205044.xyz/api/site/home
```

预期结果：

- HTTP 首页返回 `301`，Location 指向 HTTPS。
- HTTPS 用户端和管理端返回 `200 text/html`。
- API 返回 `200 application/json`，响应中的 `code` 为 `20000`。

## 17. 安全组和端口

推荐规则：

- SSH 22：只允许自己的固定 IP。
- 3000、3100：不开放，生产前端由 Nginx 提供。
- 8080：不开放，Java 只监听 `127.0.0.1`。
- 3306：不开放，MySQL 只监听 `127.0.0.1`。
- 8090：Halo 停止后不开放。
- 使用 Tunnel 时，博客运行只依赖服务器主动访问 Cloudflare；源站 80/443 可以关闭公网入站。

检查监听：

```bash
sudo ss -lntp | grep -E ':(80|443|8080|3306|3000|3100|8090)\b'
```

## 18. 完整验收清单

```bash
# 服务状态
sudo systemctl is-active blog-server mysql
sudo docker ps --filter name=charles-blog

# Java 和数据库
curl --fail http://127.0.0.1:8080/api/site/home
mysql -h 127.0.0.1 -uroot -p blog -e "SHOW TABLES;"

# Nginx 本机路由
curl --fail http://127.0.0.1/
curl --fail http://127.0.0.1/admin/
curl --fail http://127.0.0.1/api/site/home

# Tunnel
sudo docker logs --tail 100 charles-blog-cloudflared | \
  grep 'Registered tunnel connection'

# 公网
curl -I http://blog.45205044.xyz/
curl -I https://blog.45205044.xyz/
curl -I https://blog.45205044.xyz/admin/
curl https://blog.45205044.xyz/api/site/home
```

浏览器继续验证：

- 首页、文章列表、文章详情图片正常。
- 刷新 `/admin/` 和管理端子路由不返回 404。
- 管理员登录、刷新页面、退出正常。
- 新建文章后用户端能读取。
- 图片上传成功，URL 位于 `/uploads/<yyyy>/<MM>/...`。
- Network 中 API 是同域 `/api`，没有 CORS、连接拒绝或 DNSPod 跳转。

## 19. 日常运维

查看 Java：

```bash
sudo systemctl status blog-server --no-pager
sudo journalctl -u blog-server -f
tail -f /var/log/charles-blog/blog-server.log
```

重启 Java：

```bash
sudo systemctl restart blog-server
```

查看和重启 Nginx：

```bash
sudo docker logs --tail 100 charles-blog-nginx
sudo docker restart charles-blog-nginx
```

查看和重启 Tunnel：

```bash
sudo docker logs --tail 100 charles-blog-cloudflared
sudo docker restart charles-blog-cloudflared
```

更新 cloudflared：

```bash
sudo docker compose --profile tunnel -p charles-blog \
  -f /opt/charles-blog/current/deploy/docker-compose.yml \
  pull cloudflared

sudo docker compose --profile tunnel -p charles-blog \
  -f /opt/charles-blog/current/deploy/docker-compose.yml \
  up -d cloudflared
```

## 20. 发布新版本

1. 本地重新运行三个项目的测试和构建。
2. 用新的时间版本号生成 ZIP。
3. 上传、校验并解压。
4. 复制到新的 `/opt/charles-blog/releases/<版本>`。
5. 原子切换 `/opt/charles-blog/current`。
6. 重启 Java，按 Tunnel 架构重建 Nginx。

切换后执行：

```bash
sudo systemctl restart blog-server

cd /opt/charles-blog/current
sudo NGINX_CONFIG_FILE=nginx.http.conf \
  docker compose -p charles-blog -f deploy/docker-compose.yml \
  up -d --force-recreate nginx

sudo docker compose --profile tunnel -p charles-blog \
  -f deploy/docker-compose.yml up -d cloudflared
```

Flyway 只会执行尚未执行过的新迁移。已经上线的迁移 SQL 不要修改，应新建更高版本迁移。

## 21. 回滚

先查看版本：

```bash
readlink -f /opt/charles-blog/current
ls -1 /opt/charles-blog/releases
```

切换到旧版本：

```bash
OLD_VERSION=<旧版本号>
sudo ln -sfn "releases/${OLD_VERSION}" /opt/charles-blog/current.next
sudo mv -Tf /opt/charles-blog/current.next /opt/charles-blog/current
sudo systemctl restart blog-server

cd /opt/charles-blog/current
sudo NGINX_CONFIG_FILE=nginx.http.conf \
  docker compose -p charles-blog -f deploy/docker-compose.yml \
  up -d --force-recreate nginx
sudo docker compose --profile tunnel -p charles-blog \
  -f deploy/docker-compose.yml up -d cloudflared
```

代码回滚不会自动回滚数据库。包含破坏性数据库变更的版本必须提前准备数据库备份和兼容迁移方案。

## 22. 数据备份

备份 MySQL：

```bash
mkdir -p /home/ubuntu/backups
mysqldump -h 127.0.0.1 -uroot -p \
  --single-transaction --routines --triggers blog \
  | gzip > "/home/ubuntu/backups/blog-$(date +%F-%H%M%S).sql.gz"
```

备份上传文件：

```bash
sudo tar -C /var/lib/charles-blog -czf \
  "/home/ubuntu/backups/uploads-$(date +%F-%H%M%S).tar.gz" uploads
sudo chown ubuntu:ubuntu /home/ubuntu/backups/*.gz
```

部署前至少完成一次数据库与上传目录备份。

## 23. 常见故障

### 前端没有 XHR 请求

检查 `blog-web/.env.production` 是否为 `VITE_DATA_MODE=api`，修改环境文件后必须重新执行
`npm run build`，运行中的 `dist` 不会自动改变。

### `ERR_CONNECTION_REFUSED`

依次检查 Java、Nginx 和 Tunnel：

```bash
sudo systemctl status blog-server --no-pager
curl http://127.0.0.1:8080/api/site/home
curl http://127.0.0.1/api/site/home
sudo docker ps --filter name=charles-blog
```

### CORS 错误

生产前端应调用同源 `/api`。同时确认：

```env
BLOG_CORS_ALLOWED_ORIGINS=https://blog.45205044.xyz
```

修改后执行 `sudo systemctl restart blog-server`。不要把带路径的
`https://blog.45205044.xyz/admin/` 当成 Origin，Origin 只包含协议、域名和端口。

### Cloudflare 525

525 表示 Cloudflare 无法和公网源站完成 TLS 握手。本项目不再依赖公网源站 TLS，检查 DNS 是否
已经指向 Tunnel CNAME，以及 Tunnel 服务 URL 是否为 `http://127.0.0.1:80`。

### 跳转到 DNSPod 备案页

这通常表示请求仍通过旧 `blog A -> 服务器公网 IP` 到达腾讯云。删除冲突的 `blog A`，只保留
已代理的 Tunnel CNAME。不要修改其他域名记录。

### 上传文件写到了错误目录

确认：

```env
BLOG_STORAGE_ROOT=/var/lib/charles-blog/uploads
```

并检查目录所有者：

```bash
sudo chown -R ubuntu:ubuntu /var/lib/charles-blog/uploads
```

### 管理端刷新 404

确认管理端以 `/admin/` 为 Vite base 构建，并且 Nginx 的 `/admin/` 使用：

```nginx
try_files $uri $uri/ /admin/index.html;
```

## 24. 敏感信息检查

以下内容绝不能提交到 Git 或打进 ZIP：

- SSH 密码和私钥
- MySQL 密码
- `BLOG_JWT_SECRET`
- 管理员初始化密码
- Cloudflare Tunnel Token
- `/etc/charles-blog/*.env` 的真实内容

部署后建议执行：

```bash
sudo stat -c '%a %U:%G %n' /etc/charles-blog/*.env
```

所有敏感环境文件都应为 `600 root:root`。如果密码曾出现在聊天、终端历史或截图中，应立即轮换。

## 25. 当前服务器的一键安装脚本

`deploy/scripts/install.sh` 是为当前这台同时存在 Halo 和 sub2api 的服务器准备的首次迁移脚本。
它会依次完成：

1. 备份 Halo 与 sub2api 状态。
2. 停止 Halo 和 sub2api，但保留文件、容器和卷。
3. 安装并配置 MySQL。
4. 安装发布版本、种子图片及持久化目录。
5. 生成 Java 环境文件，安装 systemd 和 logrotate。
6. 启动 Java，等待 Flyway 和初始管理员完成。
7. 删除管理员初始化变量，启动 Nginx，并保留最近三个版本。

创建一次性引导配置：

```bash
sudo install -d -m 0700 /etc/charles-blog
sudoedit /etc/charles-blog/install.env
```

```env
MYSQL_ROOT_PASSWORD=<MYSQL_ROOT_PASSWORD>
BLOG_JWT_SECRET=<OPENSSL生成的随机密钥>
BLOG_ADMIN_PASSWORD=<首次管理员强密码>
```

```bash
sudo chmod 600 /etc/charles-blog/install.env
cd "${STAGING}"
sudo NGINX_CONFIG_FILE=nginx.http.conf \
  bash deploy/scripts/install.sh
```

该脚本成功后会删除 `/etc/charles-blog/install.env`，但会保留运行所需的
`/etc/charles-blog/blog-server.env`。随后仍需按第 14 至 16 节创建 Tunnel、配置 CNAME 和启用
Cloudflare HTTPS，并执行：

```bash
sudo sed -i 's/^BLOG_SECURE_COOKIE=.*/BLOG_SECURE_COOKIE=true/' \
  /etc/charles-blog/blog-server.env
sudo systemctl restart blog-server
```

纯净服务器没有 `/opt/halo`、`/home/sub2api` 或对应服务时，不要直接运行当前版本的迁移脚本，
应按本文第 7 至 16 节手动部署。
