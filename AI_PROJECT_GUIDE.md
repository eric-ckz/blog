# Charles Blog — AI 项目指南（Project Guide for AI）

> 本文件是给 **AI（或其他开发者）阅读** 的项目速览。目的是让阅读者 **无需重新分析整个仓库**，即可理解本项目的架构、技术栈、开发流程与生产部署流程。
> 如需深入某个模块，请按文中给出的文件路径直接定位。
> 最后更新：2026-09-09

---

## 1. 项目是什么

**Charles Blog** 是一个个人博客全栈项目，包含三部分：

- **用户端（blog-web）**：读者访问的博客前台，展示文章、栏目、归档、关于页，并支持访客注册、登录、发表评论。
- **管理端（blog-admin）**：博主使用的内容管理后台，支持文章、栏目、媒体、评论、站点配置管理。
- **后端（blog-server）**：Java API 服务，为两端提供数据接口与认证授权。

线上地址（生产）：
- 用户端：`https://blog.45205044.xyz/`
- 管理端：`https://blog.45205044.xyz/admin/`
- API 示例：`https://blog.45205044.xyz/api/site/home`

---

## 2. 仓库结构

```
blog/
├── blog-web/            # Vue 3 用户端（开发端口 3000）
├── blog-admin/          # Vue 3 + Element Plus 管理端（开发端口 3100）
├── blog-server/         # Java 21 + Spring Boot 4.1 API（端口 8080）
├── deploy/              # 生产部署配置（Nginx、systemd、MySQL、Cloudflare、脚本）
├── docker-compose.yml   # 本地 MySQL 容器（MySQL 8.4）
├── .env.example         # 环境变量模板（管理员、JWT、数据库、SMTP）
├── README.md            # 人类可读的项目说明（较简略）
├── deploy-staging/      # 部署暂存目录（gitignored）
├── artifacts/           # 构建产物目录（gitignored）
├── data/                # 本地数据目录（gitignored）
└── AI_PROJECT_GUIDE.md  # 本文件
```

---

## 3. 技术栈

| 模块 | 技术栈 | 说明 |
| --- | --- | --- |
| blog-web | Vue 3、Vite 6、Vue Router 4、Pinia、Axios、Tailwind CSS、Lucide、DOMPurify | 用户端，生产构建为静态文件 |
| blog-admin | Vue 3、Vite 7、Vue Router 4、Pinia、Axios、Element Plus、Tailwind CSS、TipTap、Lucide | 管理端，Vite base 为 `/admin/` |
| blog-server | Java 21、Spring Boot 4.1、Spring MVC、Spring Security、JWT（OAuth2 JOSE）、MyBatis-Plus 3.5.17、Flyway、MySQL 8 | API 服务 |
| 安全 | Argon2id（密码）、JWT 双体系（管理员 + 访客）、Refresh Token（仅存 SHA-256 哈希） | 认证授权 |
| 其它 | OWASP Java HTML Sanitizer、springdoc（Swagger UI）、BouncyCastle、Testcontainers | 内容安全、API 文档、测试 |

---

## 4. 生产架构与请求流转

```
浏览器 HTTPS
   → Cloudflare Universal SSL + Tunnel
   → cloudflared Docker 容器
   → Nginx Docker 容器（宿主机网络，监听 80）
        |-- /         → blog-web/dist（用户端静态文件）
        |-- /admin/   → blog-admin/dist（管理端，base=/admin/）
        |-- /api/     → Java 127.0.0.1:8080（反向代理）
        `-- /uploads/ → Java 127.0.0.1:8080（反向代理）
                              `→ MySQL 127.0.0.1:3306
```

- Nginx 使用 **host 网络**，因此 `blog_backend` upstream 指向 `127.0.0.1:8080`。
- Java 的 `8080` 与 MySQL 的 `3306` 只监听 `127.0.0.1`，不开放公网。
- 上传文件持久化在 `/var/lib/charles-blog/uploads`，日志在 `/var/log/charles-blog`。

---

## 5. 后端（blog-server）详解

### 5.1 包结构

```
com.eric.blog
├── BlogApplication.java        # Spring Boot 启动类
├── common/                     # BaseResponse、ErrorCode、PageRequest、PageResult、ThrowUtils
├── config/                     # SecurityConfig、JwtProperties、CorsProperties、MailProperties、StorageProperties、BootstrapAdminInitializer、MyBatisPlusConfig、OpenApiConfig、WebMvcConfig
├── controller/                 # REST 控制器
├── exception/                  # BaseException、GlobalExceptionHandler
├── mapper/                     # MyBatis-Plus Mapper 接口
├── model/
│   ├── dto/                    # 请求体（article、auth、comment）
│   ├── entity/                 # 数据库实体（Article、Category、BlogUser、Comment 等）
│   ├── enums/                  # 枚举（ArticleStatus、HomeSlotType、AboutItemType）
│   └── vo/                     # 响应体（admin/ 与 web/ 分组）
├── security/                   # JwtAuthenticationFilter（管理员）、UserJwtAuthenticationFilter（访客）、JwtTokenService、SecurityContextUtils、AdminPrincipal、UserPrincipal
├── service/                    # 业务接口 + impl/ 实现
├── storage/                    # StorageService 抽象、LocalStorageService、StorageObject、StorageType
└── utils/                      # ThrowUtils
```

### 5.2 控制器（API 表面）

管理端（需管理员 JWT，`/api/admin/**` 默认需认证）：
- `AdminAuthController` — 登录 / 刷新 / 登出
- `AdminArticleController` — 文章 CRUD（`/api/admin/articles`）
- `AdminCategoryController` — 栏目 CRUD
- `AdminCommentController` — 评论管理（分页、删除）
- `AdminDashboardController` — 工作台统计
- `AdminMediaController` — 媒体上传 / 列表 / 删除
- `AdminSiteController` — 首页配置、关于页配置
- `AdminUserController` — 账号设置（改密码等）

公开端（大部分匿名可访问）：
- `ArticleController`、`CategoryController`、`SiteController`、`CommonArticleController` — 公开博客内容
- `UserAuthController` — 访客注册 / 登录 / 刷新 / 登出 / 个人资料 / 头像（`/api/web/auth/**`）
- `CommentController` — 文章评论（`/api/common/articles/*/comments`，发表评论需登录）

### 5.3 安全模型

- **双 JWT 体系**：管理员用 `JwtAuthenticationFilter`，访客用户用 `UserJwtAuthenticationFilter`。
- **Access Token**：15 分钟有效，前端存 `localStorage`，随请求头 `Authorization: Bearer <token>` 发送。
- **Refresh Token**：7 天有效，存 HttpOnly Cookie（管理员 `blog_refresh_token`，访客 `blog_user_refresh_token`），数据库只存 SHA-256 哈希。
- **密码**：Argon2id 加密（`Argon2PasswordEncoder`）。
- **未登录访问需认证接口**：返回 401，统一错误码 `40100`。
- **CORS**：白名单来自 `blog.cors.allowed-origins`。

关键文件：
- `blog-server/src/main/java/com/eric/blog/config/SecurityConfig.java`
- `blog-server/src/main/java/com/eric/blog/security/JwtTokenService.java`
- `blog-server/src/main/java/com/eric/blog/security/UserJwtAuthenticationFilter.java`

### 5.4 数据模型（Flyway 迁移）

`blog-server/src/main/resources/db/migration/`：
- **V1__create_blog_schema.sql**：`admin_user`（管理员）、`refresh_token`（管理员刷新令牌）、`category`（栏目）、`media_asset`（媒体）、`article`（文章）、`home_article_slot`（首页文章位）、`site_setting`（站点配置）、`about_item`（关于页内容）
- **V2__seed_blog_content.sql**：5 个栏目、6 张种子图片、28 篇文章、首页槽位、站点配置、关于页示例数据
- **V3__create_user_and_comment_schema.sql**：`blog_user`（访客用户）、`email_verification_code`（邮箱验证码）、`comment`（评论）、`user_refresh_token`（访客刷新令牌）

特点：所有 ID 用 MyBatis-Plus 雪花算法生成（非自增）；所有表用 `is_delete` 逻辑删除；`utf8mb4` 字符集；每张表/字段带中文 COMMENT。

### 5.5 API 响应约定

所有接口返回统一包装（`BaseResponse`）：

```json
{
  "code": "20000",
  "message": "ok",
  "data": { ... }
}
```

- 成功：`code = "20000"`
- 错误码定义在 `com.eric.blog.common.ErrorCode`（如 `40100` 未登录、`40400` 不存在、`40000` 参数错误、`40900` 冲突等）
- 分页统一用 `PageResult` / `PageRequest`

### 5.6 配置（application*.yml）

- `blog-server/src/main/resources/application.yml` — 主配置（端口 8080、Flyway、MyBatis-Plus、springdoc、`blog.*` 各项）
- `application-dev.yml` — 开发环境，引入本地 `application-local.yml`，默认连本机 MySQL
- `application-prod.yml` — 生产环境，从环境变量读取 DB/JWT/日志，`secure-cookie: true`
- `application-test.yml` — 测试环境，用 H2（MySQL 兼容模式），Testcontainers 用于 MySQL 集成测试
- `blog-server/application-local.yml` — **本地敏感配置（gitignored）**，含 DB 密码、JWT secret、bootstrap 管理员、SMTP 模板

`blog.*` 关键配置项（见 `application.yml`）：
- `blog.jwt.*` — secret（必填，无默认值）、issuer、access-token-ttl、refresh-token-ttl、cookie 名、secure-cookie
- `blog.mail.*` — SMTP 邮件（enabled、host、port、username、password、from）
- `blog.storage.*` — 存储（type=local、root、max-size）
- `blog.cors.allowed-origins` — CORS 白名单
- `blog.bootstrap-admin.*` — 首次启动创建的管理员

### 5.7 环境变量（BLOG_*）

见根目录 `.env.example`：
- `BLOG_ADMIN_USERNAME` / `BLOG_ADMIN_PASSWORD` / `BLOG_ADMIN_DISPLAY_NAME` — 初始管理员
- `BLOG_JWT_SECRET` — JWT 密钥（至少 32 字符，生产用 `openssl rand -base64 48` 生成）
- `BLOG_DB_URL` / `BLOG_DB_USERNAME` / `BLOG_DB_PASSWORD` — MySQL
- `BLOG_STORAGE_ROOT` — 上传目录
- `BLOG_CORS_ALLOWED_ORIGINS` — CORS 来源
- `BLOG_MAIL_ENABLED` / `BLOG_MAIL_HOST` / `BLOG_MAIL_PORT` / `BLOG_MAIL_USERNAME` / `BLOG_MAIL_PASSWORD` / `BLOG_MAIL_FROM` — SMTP
- 生产环境变量文件：`/etc/charles-blog/blog-server.env`（权限 600），模板见 `deploy/blog-server.env.example`

---

## 6. 前端详解

### 6.1 blog-web（用户端，端口 3000）

- 路由（`blog-web/src/router/index.js`）：`/`（首页）、`/articles`（文章）、`/gallery`（光影集）、`/archive`（归档）、`/article/:id`（详情）、`/about`（关于）、`/login`、`/register`、`/profile`（个人中心）
- API：`src/api/article.js`、`src/api/auth.js`、`src/api/comment.js`
- 状态：`src/stores/auth.js`（访客会话，token 存 `localStorage.blog_user_access_token`）
- 仓储层：`src/repositories/`（`articleRepository.js`、`httpArticleRepository.js`、`mockArticleRepository.js`、`normalizers.js`），支持 `VITE_DATA_MODE=mock|api`
- 组件：`CommentSection.vue`（评论）、`LoadingState.vue`、`PageIntro.vue`、`SmartImage.vue`
- **注意**：`src/api/auth.js` 的 `authClient` 有请求拦截器自动附加 `Authorization: Bearer <token>`（修复 401 的关键）。

### 6.2 blog-admin（管理端，端口 3100）

- 路由（`blog-admin/src/router/index.js`）：`/login`、工作台、`/articles`、`/articles/new`、`/articles/:id/edit`、`/categories`、`/media`、`/comments`、`/site`、`/about`、`/account`
- 路由守卫：受保护页面先用 Refresh Cookie 恢复会话（`ensureSession()`），失败跳登录页
- API：`src/api/http.js`（统一 axios，带 `Authorization` 拦截器 + 401 自动刷新）、`src/api/admin.js`、`src/api/authToken.js`
- 状态：`src/stores/auth.js`
- 组件：`PageHeader.vue`、`AdminLayout.vue`
- **注意**：Vite `base` 为 `/admin/`（见 `vite.config.js`），生产由 Nginx `/admin/` 路径提供。

---

## 7. 本地开发

### 7.1 前置条件

- 本机 MySQL 已创建 `blog` 数据库
- `blog-server/application-local.yml` 配置好 DB 密码、JWT secret、bootstrap 管理员（或使用环境变量）
- Node.js（Vite 6/7）、Maven、JDK 21

### 7.2 启动命令

```powershell
# 后端（blog-server 目录）
mvn spring-boot:run

# 用户端（blog-web 目录）
npm install && npm run dev     # 端口 3000

# 管理端（blog-admin 目录）
npm install && npm run dev     # 端口 3100
```

- Flyway 首次启动自动建表 + 种子数据。
- 用户端通过 `.env.development` 使用真实 Java API。

### 7.3 本地端口

- 用户端 `3000`、管理端 `3100`、后端 `8080`、Swagger `http://localhost:8080/doc.html`

---

## 8. 测试

```powershell
# 后端（blog-server，需要 Testcontainers/Docker + H2）
mvn test

# 用户端（blog-web）
npm test          # Vitest 单元测试
npm run test:e2e  # Playwright e2e

# 管理端（blog-admin）
npm test
npm run test:e2e
```

- 后端测试文件：`BlogApiIntegrationTests`、`MySqlMigrationIntegrationTests`、`JwtTokenServiceTests`、`HtmlSanitizerServiceTests`、`StoragePropertiesTests` 等
- 前端测试：`tests/unit/`（Vitest）、`tests/e2e/`（Playwright）

---

## 9. 构建与打包

### 9.1 本地构建

```powershell
# 用户端
cd blog-web && npm ci && npm test && npm run build

# 管理端
cd blog-admin && npm ci && npm test && npm run build

# 后端
cd blog-server && mvn clean test package
```

产物：`blog-web/dist`、`blog-admin/dist`、`blog-server/target/blog-server-1.0.0.jar`

生产构建必须用同源 API（`VITE_API_BASE_URL=/api`），详见 `blog-web/.env.production`、`blog-admin/.env.production`。

### 9.2 生成部署包

```powershell
python .\deploy\scripts\package.py
```

- 在 `artifacts` 生成带版本号的 ZIP（含 dist、jar、种子图片、Nginx、systemd、MySQL 配置、部署脚本）
- 含 SHA-256 清单（`manifest.sha256`），并校验路径/权限/CRC
- **不包含**：源码、node_modules、本地数据库、真实密码

---

## 10. 生产部署流程

### 10.1 服务器（Ubuntu 24.04）需安装

- OpenJDK 21 JRE（运行 Java）
- MySQL Server 8.x（绑定 127.0.0.1:3306）
- Docker Engine + Docker Compose v2（跑 Nginx、Cloudflare Tunnel）
- systemd、logrotate、curl、ca-certificates、unzip、openssl

### 10.2 完整上线步骤（详见 `deploy/PRODUCTION_DEPLOYMENT.md`）

1. 本地构建三个项目并 `package.py` 打包
2. 上传 ZIP 到服务器（如 `/home/ubuntu/blog-deploy.zip`），解压到 `/opt/charles-blog/releases/<版本>`，`current` 原子切换到新版本
3. 初始化 MySQL（`blog` 库、utf8mb4、Flyway 自动建表）
4. 创建 `/etc/charles-blog/blog-server.env`（权限 600），配置 DB/JWT/上传/日志/生产环境变量
5. 安装 systemd 服务并启动：`systemctl enable --now blog-server`；首次启动 Flyway 建表并创建初始管理员，确认后删除初始密码并重启
6. 启动 Nginx 容器（host 网络，监听 80）：

```bash
sudo NGINX_CONFIG_FILE=nginx.http.conf docker compose -p charles-blog -f deploy/docker-compose.yml up -d nginx
```

7. 配置 Cloudflare Tunnel（把域名解析到 Cloudflare，创建 Tunnel，Public Hostname 指向 `127.0.0.1:80`），启用 HTTPS
8. 验收：`curl -I https://blog.45205044.xyz/`、管理端、API

### 10.3 版本升级/回滚

- 重新构建 → 新 release → 切换 `current` → 重启 `blog-server`、重建 Nginx
- 回滚只需把 `current` 切回旧 release
- 数据库破坏性变更需先备份（Flyway 不自动回滚）

### 10.4 部署配置位置

- Nginx：`deploy/nginx/nginx.conf`（HTTPS）、`deploy/nginx/nginx.http.conf`（HTTP 首次签证书）
- systemd：`deploy/systemd/blog-server.service`
- logrotate：`deploy/logrotate/charles-blog`
- MySQL 配置：`deploy/mysql/90-charles-blog.cnf`
- Docker Compose：`deploy/docker-compose.yml`（Nginx）
- 脚本：`deploy/scripts/`（package.py、install.sh、enable_https.sh、enable_cloudflare_tunnel.sh、remote_upload.py、remote_exec.py）

---

## 11. 关键入口文件速查

| 任务 | 文件 |
| --- | --- |
| 后端启动 | `blog-server/src/main/java/com/eric/blog/BlogApplication.java` |
| 安全配置 | `blog-server/src/main/java/com/eric/blog/config/SecurityConfig.java` |
| JWT 服务 | `blog-server/src/main/java/com/eric/blog/security/JwtTokenService.java` |
| 全局异常 | `blog-server/src/main/java/com/eric/blog/exception/GlobalExceptionHandler.java` |
| API 响应包装 | `blog-server/src/main/java/com/eric/blog/common/BaseResponse.java` |
| 数据库迁移 | `blog-server/src/main/resources/db/migration/` |
| 主配置 | `blog-server/src/main/resources/application.yml` |
| 本地配置 | `blog-server/application-local.yml`（gitignored） |
| 环境变量模板 | `.env.example` |
| 用户端路由 | `blog-web/src/router/index.js` |
| 用户端 API | `blog-web/src/api/` |
| 管理端路由 | `blog-admin/src/router/index.js` |
| 管理端 API | `blog-admin/src/api/` |
| 生产部署文档 | `deploy/PRODUCTION_DEPLOYMENT.md` |
| Cloudflare Tunnel | `deploy/CLOUDFLARE_TUNNEL.md` |

---

## 12. 重要注意事项 / 常见坑

1. **JWT secret 无默认值**：`blog.jwt.secret` 必须通过环境变量 `BLOG_JWT_SECRET` 或 `application-local.yml` 提供，否则启动失败（防止测试密钥误用于生产）。
2. **Spring Boot 4.1 较新**：MyBatis-Plus 使用 `mybatis-plus-spring-boot4-starter`；升级 Spring Boot 需注意兼容性。
3. **H2 仅测试用**：`application-test.yml` 用 H2（MySQL 模式），开发/生产统一 MySQL；`blog-server/data/blog.mv.db` 是本地 H2 残留，可删除。
4. **生产 Nginx 回源**：`nginx.conf` 注释提到 Cloudflare 回源 HTTPS 会被按 SNI 重置，当前用 80 端口回源 + Cloudflare Flexible 模式，是临时方案；备案/拦截解除后需恢复跳转。
5. **前端 `npm test`/`npm run build` 包装命令**：在某些沙箱环境会被拦截（无错误详情），可改用 `node .\node_modules\vite\bin\vite.js build` 或 `node .\node_modules\vitest\vitest.mjs run` 直接调用。
6. **头像/个人资料需登录**：`/api/web/auth/me`、`/profile`、`/avatar` 需携带 `Authorization: Bearer <token>`，前端 `authClient` 已自动附加。
7. **SMTP 授权码**：`blog.mail.password` 是邮箱服务商提供的 SMTP 授权码，不是邮箱登录密码；端口 465 走 SSL，587 走 STARTTLS。
8. **逻辑删除**：所有业务表用 `is_delete` 逻辑删除，查询时 MyBatis-Plus 自动过滤。
9. **上传限制**：Web 层 10MB/12MB，存储层还会校验真实文件大小和内容（防伪造 MIME）。
10. **管理端 base 路径**：Vite `base=/admin/`，生产必须由 Nginx `/admin/` 提供，否则刷新 404。

---

## 13. 当前开发状态（重要）

以下功能属于 **进行中的访客用户体系**，已实现但需测试验证，大量为未提交的新增文件：

- **访客注册/登录**：`blog_user`、`email_verification_code`、`user_refresh_token` 表（V3 迁移）；`UserAuthController`、`UserAuthService(Impl)`、`BlogUserMapper` 等。
- **文章评论**：`comment` 表；`CommentController`、`CommentService(Impl)`、`CommentMapper`；用户端 `CommentSection.vue`；管理端 `CommentsView.vue`。
- **管理端评论管理**：已支持按关键词（评论内容或文章标题）、文章 ID、日期范围筛选，文章列显示标题。
- **SMTP 邮件**：`MailServiceImpl` 已支持 STARTTLS(587)/SSL(465)，模板写入 `application-local.yml`，需填入真实 SMTP 授权码。

工作区有大量未提交变更（`git status` 可见），主要集中在 `blog-server` 的 user/comment 模块与前端对应页面。**在提交/部署前，请运行完整测试确认。**

---

## 14. 给后续 AI 的建议

- 阅读本文件后，如需改代码，先定位到「关键入口文件速查」对应模块。
- 新增 API 时遵循 `BaseResponse` 包装、`ErrorCode` 错误码、`@Valid` 校验、服务层分层。
- 新增表时在 `db/migration` 添加新版本 SQL（如 V4），并同步 entity/mapper/service。
- 前端新增页面时在对应 router 注册，管理端注意 base 路径。
- 安全敏感配置（密码、JWT secret、SMTP 授权码）一律走环境变量或 gitignored 的 `application-local.yml`，绝不提交到 Git。