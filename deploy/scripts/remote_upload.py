#!/usr/bin/env python3
"""Upload a deployment file or directory to the Ubuntu staging directory through SFTP.

The SSH password is read only from BLOG_SSH_PASSWORD_BASE64.  This helper deliberately
limits uploads to /home/ubuntu so privileged installation remains an explicit remote step.
"""

from __future__ import annotations

import argparse
import base64
import os
from pathlib import Path
import posixpath

import paramiko


def parse_args() -> argparse.Namespace:
    """Read the local source and an absolute staging destination on the remote host."""
    parser = argparse.ArgumentParser(description="Upload Eric Blog deployment assets through SFTP")
    parser.add_argument("--source", required=True, type=Path)
    parser.add_argument("--destination", required=True)
    parser.add_argument("--host", default="106.52.157.170")
    parser.add_argument("--user", default="ubuntu")
    return parser.parse_args()


def mkdirs(sftp: paramiko.SFTPClient, remote_directory: str) -> None:
    """Create each missing remote directory without relying on an interactive shell."""
    current = ""
    for part in remote_directory.split("/"):
        if not part:
            current = "/"
            continue
        current = posixpath.join(current, part)
        try:
            sftp.stat(current)
        except FileNotFoundError:
            sftp.mkdir(current)


def upload_file(sftp: paramiko.SFTPClient, source: Path, destination: str) -> None:
    """Upload one file through a temporary name, preserving an existing target until transfer completes."""
    mkdirs(sftp, posixpath.dirname(destination))
    temporary = f"{destination}.uploading"
    sftp.put(str(source), temporary)
    try:
        # OpenSSH supports the POSIX extension, which replaces the target atomically.
        sftp.posix_rename(temporary, destination)
    except (AttributeError, OSError):
        # Some SFTP servers do not expose POSIX rename. The completed temporary file is still
        # safer than streaming directly to the target, so replace only after put() succeeds.
        try:
            sftp.remove(destination)
        except FileNotFoundError:
            pass
        sftp.rename(temporary, destination)
    print(f"UPLOADED {source} -> {destination}")


def main() -> int:
    """Connect, validate the safe target directory, then upload the requested payload."""
    args = parse_args()
    source = args.source.resolve()
    destination = args.destination.rstrip("/")
    if not source.exists():
        raise FileNotFoundError(f"Source does not exist: {source}")
    if not destination.startswith("/home/ubuntu/") or "/../" in f"{destination}/":
        raise ValueError("Destination must be a normalized path below /home/ubuntu/")

    password_base64 = os.environ.get("BLOG_SSH_PASSWORD_BASE64")
    if not password_base64:
        raise RuntimeError("Missing BLOG_SSH_PASSWORD_BASE64")
    password = base64.b64decode(password_base64).decode("utf-8")

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(args.host, username=args.user, password=password, timeout=20,
                   banner_timeout=20, auth_timeout=20, look_for_keys=False, allow_agent=False)
    try:
        sftp = client.open_sftp()
        try:
            if source.is_file():
                upload_file(sftp, source, destination)
            else:
                for local_file in source.rglob("*"):
                    if local_file.is_file():
                        relative = local_file.relative_to(source).as_posix()
                        upload_file(sftp, local_file, posixpath.join(destination, relative))
        finally:
            sftp.close()
    finally:
        client.close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
