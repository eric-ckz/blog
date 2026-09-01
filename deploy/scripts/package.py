#!/usr/bin/env python3
"""生成可在 Linux 上安全解压的博客部署 ZIP。

Windows 自带的 Compress-Archive 会把 ZIP 条目记录为 Windows 文件，Linux 解压时可能
得到错误的目录权限。本脚本显式写入 Unix 文件类型和权限，并且只复制部署所需的构建
产物，避免把源码、node_modules、开发配置或本地数据库带入服务器。
"""

from __future__ import annotations

import argparse
import hashlib
import shutil
import stat
import sys
import zipfile
from datetime import datetime
from pathlib import Path


def parse_args() -> argparse.Namespace:
    """读取可选版本号；默认使用本地时间生成不可变发布版本。"""
    parser = argparse.ArgumentParser(description="打包 Charles Blog 生产部署文件")
    parser.add_argument("--version", help="版本号，格式 yyyyMMdd-HHmmss")
    return parser.parse_args()


def copy_tree(source: Path, target: Path) -> None:
    """复制目录，并在目标存在时拒绝覆盖，防止混入上次构建残留。"""
    if target.exists():
        raise FileExistsError(f"目标目录已存在：{target}")
    shutil.copytree(source, target)


def sha256(path: Path) -> str:
    """以流式方式计算文件摘要，避免一次性加载约 50MB 的 JAR。"""
    digest = hashlib.sha256()
    with path.open("rb") as file:
        for chunk in iter(lambda: file.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def write_manifest(staging: Path) -> None:
    """生成兼容 Linux sha256sum --check 的清单，路径统一使用正斜杠。"""
    lines = []
    for file in sorted(path for path in staging.rglob("*") if path.is_file()):
        if file.name == "manifest.sha256":
            continue
        relative = file.relative_to(staging).as_posix()
        lines.append(f"{sha256(file)}  {relative}")
    (staging / "manifest.sha256").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")


def normalize_deploy_text_files(deploy_root: Path) -> None:
    """将 Linux 直接读取的脚本和配置统一为 LF，避免 Bash 把 CR 当作命令字符。"""
    linux_text_suffixes = {".sh", ".py", ".conf", ".service", ".yml", ".cnf"}
    for file in deploy_root.rglob("*"):
        if file.is_file() and file.suffix.lower() in linux_text_suffixes:
            # 先统一 CRLF，再移除孤立 CR；Linux 配置文件不应保留任何回车字符。
            content = file.read_bytes().replace(b"\r\n", b"\n").replace(b"\r", b"")
            file.write_bytes(content)


def zip_info(relative: str, is_directory: bool) -> zipfile.ZipInfo:
    """创建带 Unix 类型位的 ZIP 条目，目录为 0755，普通文件为 0644。"""
    name = relative.rstrip("/") + ("/" if is_directory else "")
    info = zipfile.ZipInfo(name, date_time=datetime.now().timetuple()[:6])
    info.create_system = 3  # 3 表示 Unix；Linux unzip 才会采用 external_attr 中的权限。
    mode = (stat.S_IFDIR | 0o755) if is_directory else (stat.S_IFREG | 0o644)
    info.external_attr = mode << 16
    if is_directory:
        info.external_attr |= 0x10
        info.compress_type = zipfile.ZIP_STORED
    else:
        info.compress_type = zipfile.ZIP_DEFLATED
    return info


def create_zip(staging: Path, output: Path) -> None:
    """按目录优先顺序写入 ZIP，确保每层目录都有明确、可执行的 Unix 权限。"""
    with zipfile.ZipFile(output, "w", allowZip64=True) as archive:
        entries = sorted(staging.rglob("*"), key=lambda path: (len(path.parts), path.as_posix()))
        for path in entries:
            relative = path.relative_to(staging).as_posix()
            if path.is_dir():
                archive.writestr(zip_info(relative, True), b"")
            else:
                info = zip_info(relative, False)
                with path.open("rb") as source, archive.open(info, "w") as target:
                    shutil.copyfileobj(source, target, length=1024 * 1024)


def verify_zip(output: Path) -> None:
    """重新读取 ZIP，校验路径、CRC、Unix 权限和本地配置未被打包。"""
    forbidden_fragments = ("\\", "node_modules/", "/.env", "application-local.yml", "data/")
    with zipfile.ZipFile(output, "r") as archive:
        bad_names = [item.filename for item in archive.infolist() if any(x in item.filename for x in forbidden_fragments)]
        if bad_names:
            raise RuntimeError(f"ZIP 包含非法路径：{bad_names[:5]}")
        for item in archive.infolist():
            if item.create_system != 3:
                raise RuntimeError(f"条目不是 Unix 格式：{item.filename}")
            expected = 0o755 if item.is_dir() else 0o644
            actual = (item.external_attr >> 16) & 0o777
            if actual != expected:
                raise RuntimeError(f"权限错误：{item.filename} 为 {oct(actual)}，期望 {oct(expected)}")
        corrupt = archive.testzip()
        if corrupt:
            raise RuntimeError(f"ZIP CRC 校验失败：{corrupt}")


def main() -> int:
    """组装部署目录、生成清单并输出最终 ZIP 的路径和摘要。"""
    args = parse_args()
    version = args.version or datetime.now().strftime("%Y%m%d-%H%M%S")
    try:
        datetime.strptime(version, "%Y%m%d-%H%M%S")
    except ValueError as error:
        raise ValueError("版本号必须使用 yyyyMMdd-HHmmss 格式") from error

    project_root = Path(__file__).resolve().parents[2]
    web_dist = project_root / "blog-web" / "dist"
    admin_dist = project_root / "blog-admin" / "dist"
    server_jar = project_root / "blog-server" / "target" / "blog-server-1.0.0.jar"
    seed_uploads = project_root / "blog-server" / "uploads" / "2026" / "07" / "seed"
    for required in (web_dist, admin_dist, server_jar, seed_uploads):
        if not required.exists():
            raise FileNotFoundError(f"缺少构建产物：{required}")

    staging = project_root / "deploy-staging" / f"blog-deploy-{version}"
    artifacts = project_root / "artifacts"
    output = artifacts / f"blog-deploy-{version}.zip"
    if staging.exists() or output.exists():
        raise FileExistsError("同版本部署目录或 ZIP 已存在，请使用新的版本号")

    staging.mkdir(parents=True)
    artifacts.mkdir(parents=True, exist_ok=True)
    copy_tree(web_dist, staging / "web")
    copy_tree(admin_dist, staging / "admin")
    copy_tree(project_root / "deploy", staging / "deploy")
    normalize_deploy_text_files(staging / "deploy")
    copy_tree(seed_uploads, staging / "seed-uploads")
    (staging / "server").mkdir()
    shutil.copy2(server_jar, staging / "server" / "blog-server.jar")
    (staging / "VERSION").write_text(version + "\n", encoding="utf-8", newline="\n")
    write_manifest(staging)
    create_zip(staging, output)
    verify_zip(output)

    print(f"VERSION={version}")
    print(f"ZIP={output}")
    print(f"SIZE={output.stat().st_size}")
    print(f"SHA256={sha256(output)}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:  # 输出简洁错误给打包操作者，完整异常由非零退出码表达。
        print(f"打包失败：{error}", file=sys.stderr)
        raise
