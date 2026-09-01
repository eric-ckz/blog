# 云服务器部署说明

完整的从零部署和日常运维流程见 `PRODUCTION_DEPLOYMENT.md`。本文件只保留部署包和当前服务器
迁移脚本的简要说明。

部署包由本地构建流程生成，不包含数据库密码、JWT 密钥或 SSH 密码。上传路径固定为：

```text
/home/ubuntu/blog-deploy.zip
```

完成三个项目的测试和生产构建后，在项目根目录执行：

```powershell
python deploy/scripts/package.py
```

脚本会在 `artifacts` 下生成带版本号的 ZIP，并输出文件大小与 SHA-256。它会显式设置
Linux 目录权限 0755、文件权限 0644，同时拒绝打包本地配置和 Windows 反斜杠路径。

服务器端会先校验 `manifest.sha256`，再备份并停止 Halo 与 sub2api。Nginx 使用 Docker
host 网络监听 80：根路径提供用户端，`/admin/` 提供管理端。Java 由 systemd 以 `ubuntu`
用户监听本机 8080，MySQL 只监听本机。

恢复旧服务：

```bash
# sub2api 和博客后端都需要宿主机 8080，恢复前先停止博客后端。
sudo systemctl disable --now blog-server
cd /home/sub2api && docker compose start

# Halo 使用独立的 8090，可单独恢复。
sudo systemctl enable --now halo
```

部署完成后应立即在管理端修改初始密码。接入 Cloudflare HTTPS 后必须保持
`BLOG_SECURE_COOKIE=true`。
