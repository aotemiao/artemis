#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

python3 - <<'PY'
from pathlib import Path
import re
import sys

REPO = Path.cwd()
TARGETS = [
    (
        Path("artemis-modules/artemis-system/artemis-system-client/src/main/java/com/aotemiao/artemis/system/client/api/UserValidateService.java"),
        Path("artemis-modules/artemis-system/artemis-system-client/src/main/java/com/aotemiao/artemis/system/client/dto/ValidateCredentialsRequest.java"),
        Path("artemis-modules/artemis-system/artemis-system-client/CLIENT_CONTRACT.md"),
    )
]


def normalize_signature(value: str) -> str:
    return " ".join(value.strip().strip("`").split())


def parse_package(java_text: str) -> str:
    match = re.search(r'^package\s+([a-zA-Z0-9_.]+);$', java_text, re.MULTILINE)
    if not match:
        raise ValueError("missing package declaration")
    return match.group(1)


def parse_interface_signatures(java_path: Path) -> tuple[str, set[str]]:
    text = java_path.read_text(encoding="utf-8")
    package_name = parse_package(text)
    interface_match = re.search(r'public\s+interface\s+(\w+)', text)
    if not interface_match:
        raise ValueError(f"{java_path} is missing public interface declaration")
    interface_name = interface_match.group(1)
    methods = {
        normalize_signature(match.group(1))
        for match in re.finditer(r'^\s*([A-Z][^;=]+\)\s*);$', text, re.MULTILINE)
    }
    return f"{package_name}.{interface_name}", methods


def parse_dto_signature(java_path: Path) -> str:
    text = java_path.read_text(encoding="utf-8")
    package_name = parse_package(text)
    record_match = re.search(r'public\s+record\s+(\w+)\(', text, re.MULTILINE)
    if not record_match:
        raise ValueError(f"{java_path} is missing public record declaration")
    record_name = record_match.group(1)
    start = record_match.end()
    depth = 1
    index = start
    while index < len(text) and depth > 0:
        char = text[index]
        if char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
        index += 1
    raw_args = text[start:index - 1].replace("\n", " ")
    raw_args = re.sub(r'@\w+(?:\([^)]*\))?\s*', "", raw_args)
    record_args = normalize_signature(raw_args)
    return f"{package_name}.{record_name}({record_args})"


def parse_doc_entries(doc_path: Path) -> tuple[set[str], set[str], set[str]]:
    text = doc_path.read_text(encoding="utf-8")
    interfaces = {
        normalize_signature(match.group(1))
        for match in re.finditer(r'INTERFACE:\s*(.+)', text)
    }
    methods = {
        normalize_signature(match.group(1))
        for match in re.finditer(r'METHOD:\s*(.+)', text)
    }
    dtos = {
        normalize_signature(match.group(1))
        for match in re.finditer(r'DTO:\s*(.+)', text)
    }
    return interfaces, methods, dtos


errors: list[str] = []
for interface_path, dto_path, doc_path in TARGETS:
    interface_name, methods = parse_interface_signatures(REPO / interface_path)
    dto_signature = parse_dto_signature(REPO / dto_path)
    doc_interfaces, doc_methods, doc_dtos = parse_doc_entries(REPO / doc_path)

    if interface_name not in doc_interfaces:
        errors.append(f"missing interface doc entry: {interface_name}")
    for method in sorted(methods):
        if method not in doc_methods:
            errors.append(f"missing method doc entry: {method}")
    if dto_signature not in doc_dtos:
        errors.append(f"missing dto doc entry: {dto_signature}")

if errors:
    print("Client contract sync check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    sys.exit(1)

print("Client contract sync check passed.")
PY
