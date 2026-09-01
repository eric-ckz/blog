# Charles Blog

一个包含用户端、内容管理端和 Java API 服务的个人博客项目。

## 项目结构

```text
blog-web/       Vue 3 用户端，默认端口 3000
blog-admin/     Vue 3 + Element Plus 管理端，默认端口 3100
blog-server/    Java 21 + Spring Boot 4 API，默认端口 8080
```

## 技术栈

项目由一个用户端、一个管理端和一个后端 API 服务组成。两个前端在生产环境构建为静态文件，
由 Nginx 提供，不需要在服务器上长期运行 Node.js 服务。

| 项目 | 技术栈 | 主要职责 |
| --- | --- | --- |
| `blog-web` 用户端 | Vue 3、Vite 6、Vue Router 4、Axios、Tailwind CSS、Lucide、DOMPurify | 博客首页、栏目、文章列表、归档、关于页和文章详情 |
| `blog-admin` 管理端 | Vue 3、Vite 7、Vue Router 4、Pinia、Axios、Element Plus、Tailwind CSS、TipTap、Lucide | 管理员登录、文章与栏目管理、媒体上传、站点配置 |
| `blog-server` 后端 | Java 21、Spring Boot 4.1、Spring MVC、Spring Security、JWT/OAuth2 JOSE、MyBatis-Plus 3.5.17、Flyway、MySQL 8 | 公开博客 API、管理端 CRUD、认证授权、文件上传和数据持久化 |

后端还使用 OWASP HTML Sanitizer 清理富文本、Argon2id 加密管理员密码，并通过
`StorageService` 预留 MinIO/S3 存储扩展。H2 只用于自动化测试，开发和生产统一使用 MySQL；
Testcontainers 用于后端集成测试。前端测试使用 Vitest、Vue Test Utils/JS DOM 和 Playwright。

## 生产架构与地址

```text
浏览器 HTTPS
    -> Cloudflare Universal SSL + Tunnel
    -> cloudflared Docker 容器
    -> Nginx Docker 容器（宿主机网络，监听 80）
         |-- /         -> blog-web/dist
         |-- /admin/   -> blog-admin/dist
         |-- /api/     -> Java 127.0.0.1:8080
         `-- /uploads/ -> Java 127.0.0.1:8080
                              `-> MySQL 127.0.0.1:3306
```

线上地址为：

- 用户端：`https://blog.45205044.xyz/`
- 管理端：`https://blog.45205044.xyz/admin/`
- API 示例：`https://blog.45205044.xyz/api/site/home`

本地开发端口仍为用户端 `3000`、管理端 `3100`、后端 `8080`。生产环境只对外提供
Cloudflare 的 HTTPS；Java 的 `8080` 和 MySQL 的 `3306` 只监听 `127.0.0.1`，不应开放公网。
上传文件持久化在 `/var/lib/charles-blog/uploads`，日志在 `/var/log/charles-blog`。

## 云服务器需要安装的中间件

当前部署目标为 Ubuntu 24.04。服务器需要安装以下基础软件和运行时：

| 软件 | 用途 |
| --- | --- |
| OpenJDK 21 JRE | 运行 `blog-server`；Maven 只在本地构建时需要 |
| MySQL Server 8.x | 保存文章、栏目、管理员和站点配置，绑定 `127.0.0.1:3306` |
| Docker Engine、Docker Compose v2 | 运行 Nginx 和 Cloudflare Tunnel 容器 |
| `nginx:1.28-alpine` | 托管两个前端静态目录并代理 `/api`、`/uploads` |
| `cloudflare/cloudflared` | 将域名 HTTPS 流量通过 Tunnel 转发到本机 Nginx |
| systemd、logrotate | Java 服务开机启动、自动重启和日志轮转 |
| curl、ca-certificates、unzip、OpenSSL | 健康检查、解包、证书链和生成 JWT 密钥 |

在全新 Ubuntu 服务器上可以执行：

```bash
sudo apt-get update
sudo apt-get install -y \
  openjdk-21-jre-headless \
  docker.io docker-compose-v2 \
  mysql-server curl ca-certificates unzip openssl

sudo systemctl enable --now docker mysql
java -version
docker version
docker compose version
mysql --version
```

Node.js、npm、Maven 和 Python 是本地构建工具，不需要为了运行生产静态文件而安装在服务器上。

## 从构建到上线

下面是完整流程的概要；数据库字段、权限、备份、升级和故障恢复的逐条命令见
[`deploy/PRODUCTION_DEPLOYMENT.md`](deploy/PRODUCTION_DEPLOYMENT.md)。

### 1. 本地配置并构建

生产构建必须使用同源 API，不能把 `localhost:8080` 或服务器 IP 写进前端：

```env
# blog-web/.env.production
VITE_DATA_MODE=api
VITE_API_BASE_URL=/api

# blog-admin/.env.production
VITE_API_BASE_URL=/api
VITE_BLOG_WEB_URL=https://blog.45205044.xyz/
```

在项目根目录执行测试和生产构建：

```powershell
Set-Location .\blog-web
npm ci
npm test
npm run build
Set-Location ..

Set-Location .\blog-admin
npm ci
npm test
npm run build
Set-Location ..

Set-Location .\blog-server
mvn clean test package
Set-Location ..
```

构建产物分别是 `blog-web/dist`、`blog-admin/dist` 和
`blog-server/target/blog-server-1.0.0.jar`。管理端的 Vite base 是 `/admin/`，因此必须由
Nginx 的 `/admin/` 路径提供。

### 2. 生成部署包并上传

```powershell
python .\deploy\scripts\package.py
```

脚本会在 `artifacts` 生成带版本号的 ZIP，包含两个 `dist`、Java JAR、种子图片、Nginx、
Docker Compose、systemd、MySQL 和部署脚本，不包含源码、`node_modules`、本地数据库或真实密码。
将 ZIP 上传到服务器：

```text
/home/ubuntu/blog-deploy.zip
```

登录服务器后读取 ZIP 中的版本号，解压到暂存目录，并先校验清单：

```bash
VERSION="$(unzip -p /home/ubuntu/blog-deploy.zip VERSION | tr -d '\r\n')"
STAGING="/home/ubuntu/blog-deploy-${VERSION}"
mkdir -p "${STAGING}"
unzip /home/ubuntu/blog-deploy.zip -d "${STAGING}"
cd "${STAGING}"
sha256sum --check manifest.sha256
```

所有文件显示 `OK` 后，再复制到 `/opt/charles-blog/releases/<版本>`，并把
`/opt/charles-blog/current` 原子切换到新版本。上传目录和日志目录必须放在版本目录之外，
这样升级或回滚不会丢失业务数据。

### 3. 初始化 MySQL

MySQL 安装后配置 `blog` 数据库、`utf8mb4` 字符集和 root 密码，监听地址保持
`127.0.0.1:3306`，不要开放安全组 3306。项目提供的 Flyway 迁移会在 Java 首次启动时自动创建
表、字段注释、索引和演示数据。数据库密码只写入服务器的 root-only 环境文件，不写进 Git 或 ZIP。

### 4. 配置并启动 Java API

创建权限为 `600` 的 `/etc/charles-blog/blog-server.env`，至少配置以下变量（尖括号内容由部署者填写）：

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_ADDRESS=127.0.0.1
SERVER_PORT=8080
BLOG_DB_URL=jdbc:mysql://127.0.0.1:3306/blog?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
BLOG_DB_USERNAME=root
BLOG_DB_PASSWORD=<MYSQL_ROOT_PASSWORD>
BLOG_JWT_SECRET=<openssl rand -base64 48 生成的随机值>
BLOG_STORAGE_ROOT=/var/lib/charles-blog/uploads
BLOG_LOG_FILE=/var/log/charles-blog/blog-server.log
BLOG_CORS_ALLOWED_ORIGINS=https://blog.45205044.xyz
BLOG_SECURE_COOKIE=true
```

安装项目中的 systemd 和 logrotate 配置后启动：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now blog-server
sudo systemctl status blog-server --no-pager
curl --fail http://127.0.0.1:8080/api/site/home
```

首次启动会执行 Flyway 并创建初始化管理员。确认创建成功后，应从环境文件删除初始化管理员密码，
重启服务，并立即在管理端修改初始密码。

### 5. 启动 Docker Nginx

Nginx 使用 host 网络监听本机 80：根路径提供用户端，`/admin/` 提供管理端，`/api/` 和
`/uploads/` 代理到 `127.0.0.1:8080`。在当前版本目录执行：

```bash
cd /opt/charles-blog/current
sudo NGINX_CONFIG_FILE=nginx.http.conf \
  docker compose -p charles-blog -f deploy/docker-compose.yml up -d nginx

curl --fail http://127.0.0.1/
curl --fail http://127.0.0.1/admin/
curl --fail http://127.0.0.1/api/site/home
```

生产环境不需要运行 `npm run dev`，也不需要开放 3000、3100。Nginx 已配置 Vue History 回退、
12MB 上传限制、静态资源缓存和安全响应头。

### 6. 配置 Cloudflare HTTPS

在 Cloudflare 中将域名接入并创建 `charles-blog` Tunnel：

1. 将域名注册商的 NS 修改为 Cloudflare 分配的名称服务器。
2. 删除 `blog.45205044.xyz` 旧的 A/AAAA 记录，避免请求继续到旧源站。
3. 创建 Tunnel 的 Docker 连接器，把 Tunnel Token 写入服务器的
   `/etc/charles-blog/cloudflared.env`（权限 `600`），启动 `cloudflare/cloudflared` 容器。
4. 在 Public Hostname 添加 `blog.45205044.xyz`，Service 选择 `HTTP`，URL 填
   `127.0.0.1:80`。
5. 确认 DNS 为已代理的 Tunnel CNAME，开启 Universal SSL 和“始终使用 HTTPS”。

浏览器到 Cloudflare 使用 HTTPS，Tunnel 到本机 Nginx 的 loopback 回源可以使用 HTTP，二者不会
形成重定向循环。证书由 Cloudflare 自动签发和轮换，不需要把证书或 Tunnel Token 提交到仓库。

### 7. 上线验收

```bash
sudo systemctl is-active blog-server mysql
sudo docker ps --filter name=charles-blog
curl -I https://blog.45205044.xyz/
curl -I https://blog.45205044.xyz/admin/
curl https://blog.45205044.xyz/api/site/home
```

浏览器中还应验证管理员登录、Refresh Cookie、文章增删改、图片上传、管理端子路由刷新和用户端
文章详情。出现连接拒绝、CORS、Cloudflare 525 或上传目录错误时，按部署手册的“常见故障”章节排查。

### 8. 升级、回滚与恢复

升级时重新构建三个项目、生成新部署包、校验后创建新的 release，再切换 `current` 并重启
`blog-server`、重建 Nginx。回滚只需把 `current` 切换到旧 release；数据库迁移不会自动回滚，
因此破坏性数据库变更必须先备份。Halo 和 sub2api 若仍保留在服务器上，恢复前先停止博客后端，
再分别执行原服务的 systemd 或 Docker Compose 启动命令。

## 本地启动

1. 确认本机 MySQL 已创建 `blog` 数据库；开发账号默认是 `root`，连接信息可在 `blog-server/application-local.yml` 中调整。
2. 复制根目录 `.env.example` 中的管理员变量到系统环境变量，或写入本地配置，并设置安全的管理员密码与 JWT 密钥。
3. 在 `blog-server` 中执行 `mvn spring-boot:run`。
4. 在 `blog-web` 中执行 `npm install && npm run dev`。
5. 在 `blog-admin` 中执行 `npm install && npm run dev`。

开发环境和生产环境均使用 MySQL，开发环境默认连接
`jdbc:mysql://127.0.0.1:3306/blog`。H2 仅用于自动化测试，测试不会读写本地 MySQL。
Flyway 会在第一次启动时自动创建数据表、表字段、索引和初始演示数据。
用户端执行 `npm run dev` 时会通过 `.env.development` 自动使用真实 Java API；
环境变量只在 Vite 启动时读取，修改后需要重启开发服务。

## 数据模式

用户端继续支持 `VITE_DATA_MODE=mock|api`。后端启动后推荐使用：

```env
VITE_DATA_MODE=api
VITE_API_BASE_URL=/api
```

上传文件默认保存在 `blog-server/uploads/{yyyy}/{MM}`，存储层已经通过
`StorageService` 预留 MinIO/S3 扩展能力。

## 常用验证命令

```powershell
cd blog-server
mvn test

cd ..\blog-web
npm test
npm run build
npm run test:e2e

cd ..\blog-admin
npm test
npm run build
npm run test:e2e
```

用户端开发地址为 `http://localhost:3000`，管理端为 `http://localhost:3100`，
后端 API 与 Swagger UI 分别为 `http://localhost:8080/api` 和
`http://localhost:8080/doc.html`。

## 云服务器部署

生产部署配置位于 `deploy`，采用 Cloudflare Tunnel + Docker Nginx + systemd Java + 本机 MySQL。
线上用户端位于 `https://blog.45205044.xyz/`，管理端位于 `/admin/`。部署包不包含任何密码，
敏感配置只在服务器 `/etc/charles-blog` 中生成。完整的构建、上传、数据库、Nginx、Tunnel、
HTTPS、升级及回滚流程见 `deploy/PRODUCTION_DEPLOYMENT.md`。
