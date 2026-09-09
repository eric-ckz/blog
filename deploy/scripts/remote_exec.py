#!/usr/bin/env python3
"""通过 SSH 执行部署脚本。

密码只从进程环境变量读取，不写入源码或部署包。远端脚本使用 Base64 传入，避免
PowerShell、Python 与 Bash 多层引号互相干扰。该工具会输出服务器主机密钥指纹，
便于部署日志保留首次连接证据。
"""

from __future__ import annotations

import argparse
import base64
import hashlib
import os
import sys

import paramiko


def parse_args() -> argparse.Namespace:
    """读取连接信息和脚本；敏感密码刻意不允许通过命令行参数传递。"""
    parser = argparse.ArgumentParser(description="执行 Eric Blog 远程部署命令")
    parser.add_argument("--host", default="106.52.157.170")
    parser.add_argument("--user", default="ubuntu")
    parser.add_argument("--script-base64", required=True)
    parser.add_argument("--sudo", action="store_true")
    parser.add_argument("--timeout", type=int, default=1800)
    return parser.parse_args()


def main() -> int:
    """连接服务器、执行 Bash，并原样转发 stdout、stderr 和退出码。"""
    args = parse_args()
    password_base64 = os.environ.get("BLOG_SSH_PASSWORD_BASE64")
    if not password_base64:
        raise RuntimeError("缺少 BLOG_SSH_PASSWORD_BASE64")
    password = base64.b64decode(password_base64).decode("utf-8")
    script = base64.b64decode(args.script_base64).decode("utf-8")

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(
        hostname=args.host,
        username=args.user,
        password=password,
        timeout=20,
        banner_timeout=20,
        auth_timeout=20,
        look_for_keys=False,
        allow_agent=False,
    )
    key = client.get_transport().get_remote_server_key()
    fingerprint = base64.b64encode(hashlib.sha256(key.asbytes()).digest()).decode("ascii").rstrip("=")
    print(f"SSH_HOST_KEY_SHA256={fingerprint}", flush=True)

    channel = client.get_transport().open_session(timeout=20)
    channel.settimeout(args.timeout)
    # 当前云主机的 ubuntu 账号配置了免密 sudo。使用 -n 可保证权限配置变化时立即失败，
    # 避免密码与待执行脚本共用标准输入后被 Bash 误当成命令。
    command = "sudo -n bash -s" if args.sudo else "bash -s"
    channel.exec_command(command)
    channel.sendall(script.encode("utf-8"))
    channel.shutdown_write()

    stdout = channel.makefile("rb", -1).read()
    stderr = channel.makefile_stderr("rb", -1).read()
    exit_code = channel.recv_exit_status()
    sys.stdout.buffer.write(stdout)
    sys.stderr.buffer.write(stderr)
    client.close()
    return exit_code


if __name__ == "__main__":
    raise SystemExit(main())
