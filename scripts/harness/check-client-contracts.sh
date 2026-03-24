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


def normalize_signature(value: str) -> str:
    return " ".join(value.strip().strip("`").split())


def parse_package(java_text: str) -> str:
    match = re.search(r'^package\s+([a-zA-Z0-9_.]+);$', java_text, re.MULTILINE)
    if not match:
        raise ValueError("missing package declaration")
    return match.group(1)


def parse_interface_signatures(java_path: Path) -> tuple[str, set[str]]:
    text = java_path.read_text(encoding="utf-8")
    text = re.sub(r'/\*.*?\*/', '', text, flags=re.S)
    text = re.sub(r'//.*$', '', text, flags=re.M)
    package_name = parse_package(text)
    interface_match = re.search(r'public\s+interface\s+(\w+)', text)
    if not interface_match:
        raise ValueError(f"{java_path} is missing public interface declaration")
    interface_name = interface_match.group(1)
    methods = {
        normalize_signature(match.group(1))
        for match in re.finditer(r'^\s*(?!default\b)(?!static\b)([^;={}]+\([^;={}]*\)\s*);$', text, re.MULTILINE)
    }
    return f"{package_name}.{interface_name}", methods


def parse_dto_signature(java_path: Path) -> str:
    text = java_path.read_text(encoding="utf-8")
    package_name = parse_package(text)
    record_match = re.search(r'public\s+record\s+(\w+)\(', text, re.MULTILINE)
    if record_match:
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

    class_match = re.search(r'public\s+class\s+(\w+)', text, re.MULTILINE)
    if class_match:
        return f"{package_name}.{class_match.group(1)}"

    raise ValueError(f"{java_path} is missing public record/class declaration")


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
client_modules = sorted(REPO.glob("artemis-modules/artemis-*/artemis-*-client"))
for module_dir in client_modules:
    doc_path = module_dir / "CLIENT_CONTRACT.md"
    if not doc_path.is_file():
        errors.append(f"missing client contract doc: {doc_path.relative_to(REPO)}")
        continue

    interfaces = sorted((module_dir / "src/main/java").glob("**/client/api/*.java"))
    dtos = sorted((module_dir / "src/main/java").glob("**/client/dto/*.java"))
    doc_interfaces, doc_methods, doc_dtos = parse_doc_entries(doc_path)

    expected_interfaces: set[str] = set()
    expected_methods: set[str] = set()
    expected_dtos: set[str] = set()

    for interface_path in interfaces:
        interface_name, methods = parse_interface_signatures(interface_path)
        expected_interfaces.add(interface_name)
        expected_methods.update(methods)

    for dto_path in dtos:
        expected_dtos.add(parse_dto_signature(dto_path))

    missing_interfaces = sorted(expected_interfaces - doc_interfaces)
    missing_methods = sorted(expected_methods - doc_methods)
    missing_dtos = sorted(expected_dtos - doc_dtos)
    extra_interfaces = sorted(doc_interfaces - expected_interfaces)
    extra_methods = sorted(doc_methods - expected_methods)
    extra_dtos = sorted(doc_dtos - expected_dtos)

    if missing_interfaces or missing_methods or missing_dtos or extra_interfaces or extra_methods or extra_dtos:
        errors.append(f"client contract mismatch: {module_dir.relative_to(REPO)}")
        for value in missing_interfaces:
            errors.append(f"  missing interface doc entry: {value}")
        for value in missing_methods:
            errors.append(f"  missing method doc entry: {value}")
        for value in missing_dtos:
            errors.append(f"  missing dto doc entry: {value}")
        for value in extra_interfaces:
            errors.append(f"  extra interface doc entry: {value}")
        for value in extra_methods:
            errors.append(f"  extra method doc entry: {value}")
        for value in extra_dtos:
            errors.append(f"  extra dto doc entry: {value}")

if errors:
    print("Client contract sync check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    sys.exit(1)

print("Client contract sync check passed.")
PY
