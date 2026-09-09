# Cloudflare Tunnel 运维说明

公开 HTTPS 证书没有“永久不过期”这一类型。Cloudflare Tunnel 使用 Cloudflare 托管的边缘证书，
平台会在过期前自动轮换；服务器无需执行 Certbot 续签命令。

## 首次启用

1. 在 Cloudflare Zero Trust 的 `Networks -> Tunnels` 创建名为 `eric-blog` 的 Tunnel。
2. 复制 Docker 安装命令中 `--token` 后面的 token。
3. 在服务器执行以下命令，避免 token 进入 Shell 历史：

```bash
read -rsp 'Tunnel token: ' CLOUDFLARE_TUNNEL_TOKEN; echo
export CLOUDFLARE_TUNNEL_TOKEN
sudo --preserve-env=CLOUDFLARE_TUNNEL_TOKEN \
  bash /opt/eric-blog/current/deploy/scripts/enable_cloudflare_tunnel.sh
unset CLOUDFLARE_TUNNEL_TOKEN
```

4. 在 Tunnel 的 `Public Hostnames` 中增加：

```text
Hostname: blog.45205044.xyz
Service type: HTTP
URL: 127.0.0.1:80
```

Tunnel 到 Nginx 的连接只经过服务器本机 loopback，因此内部使用 HTTP；浏览器到 Cloudflare 仍使用公开受信任、
自动轮换的 HTTPS 证书。添加 Public Hostname 前需删除与 `blog` 冲突的旧 A/AAAA/CNAME 记录，并确保最终
DNS 记录为已代理的 CNAME：

```text
blog -> <tunnel-id>.cfargotunnel.com
```

不要修改 `api` 或其他业务域名的记录。

5. 在域名的 `SSL/TLS -> 边缘证书` 中启用“始终使用 HTTPS”。HTTP 请求应由 Cloudflare 返回 301，
   Tunnel 内部仍使用 `127.0.0.1:80`，两者不会形成重定向循环。

## 日常检查与恢复

```bash
# 查看 Tunnel 连接，正常日志应包含 Registered tunnel connection。
sudo docker logs --tail 100 eric-blog-cloudflared

# 重启 Tunnel。
sudo docker restart eric-blog-cloudflared

# 更新 cloudflared 镜像并重建容器；不会更换证书或 Tunnel 身份。
sudo docker compose --profile tunnel -p eric-blog \
  -f /opt/eric-blog/current/deploy/docker-compose.yml pull cloudflared
sudo docker compose --profile tunnel -p eric-blog \
  -f /opt/eric-blog/current/deploy/docker-compose.yml up -d cloudflared

# 更换 Tunnel token。执行后重新运行首次启用脚本即可。
sudo rm -f /etc/eric-blog/cloudflared.env
```

只要 Tunnel 容器在线，Cloudflare 会自动管理边缘证书，不需要 `certbot renew`。若未来完成域名备案并
恢复正常公网 SNI，也可以改回 Let’s Encrypt；此项目的 `enable_https.sh` 会负责签发，续签 timer
会执行 `certbot renew`。
