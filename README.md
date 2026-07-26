# Charles Blog

一个包含用户端、内容管理端和 Java API 服务的个人博客项目。

## 项目结构

```text
blog-web/       Vue 3 用户端，默认端口 3000
blog-admin/     Vue 3 + Element Plus 管理端，默认端口 3100
blog-server/    Java 21 + Spring Boot 4 API，默认端口 8080
```

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
