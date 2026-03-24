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
        Path("artemis-auth/src/main/java/com/aotemiao/artemis/auth/web/AuthController.java"),
        Path("artemis-auth/AUTH_API.md"),
    ),
    (
        Path("artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web/LookupTypeController.java"),
        Path("artemis-modules/artemis-system/LOOKUP_API.md"),
    ),
    (
        Path("artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web/InternalAuthController.java"),
        Path("artemis-modules/artemis-system/INTERNAL_AUTH_API.md"),
    ),
    (
        Path("artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web/SystemUserController.java"),
        Path("artemis-modules/artemis-system/USER_API.md"),
    ),
    (
        Path("artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web/SystemRoleController.java"),
        Path("artemis-modules/artemis-system/ROLE_API.md"),
    ),
]

for service_doc in sorted(REPO.glob("artemis-modules/artemis-*/SERVICE_API.md")):
    service_root = service_doc.parent
    controller_candidates = sorted(service_root.glob("artemis-*-adapter/src/main/java/**/adapter/web/*PingController.java"))
    if len(controller_candidates) != 1:
        continue
    TARGETS.append((controller_candidates[0].relative_to(REPO), service_doc.relative_to(REPO)))


def normalize_signature(value: str) -> str:
    return " ".join(value.strip().strip("`").split())


def resolve_mapping_arg(raw: str, constants: dict[str, str]) -> str:
    value = raw.strip()
    if not value:
        return ""
    if "=" in value:
        _, value = value.split("=", 1)
        value = value.strip()
    if value.startswith('"') and value.endswith('"'):
        return value[1:-1]
    if value in constants:
        return constants[value]
    tail = value.rsplit(".", 1)[-1]
    if tail in constants:
        return constants[tail]
    raise ValueError(f"Unsupported mapping expression: {raw}")


def join_paths(base: str, child: str) -> str:
    if not child:
        return base or "/"
    if not base:
        return child if child.startswith("/") else f"/{child}"
    return f"{base.rstrip('/')}/{child.lstrip('/')}"


def parse_routes(controller_path: Path) -> set[str]:
    text = controller_path.read_text(encoding="utf-8")
    constants = {
        match.group(1): match.group(2)
        for match in re.finditer(r'public\s+static\s+final\s+String\s+(\w+)\s*=\s*"([^"]+)";', text)
    }
    class_mapping_match = re.search(r'@RequestMapping\(([^)]*)\)\s*public\s+class', text, re.MULTILINE)
    base_path = ""
    if class_mapping_match:
        base_path = resolve_mapping_arg(class_mapping_match.group(1), constants)

    pattern = re.compile(
        r'@(?P<method>GetMapping|PostMapping|PutMapping|DeleteMapping)(?:\((?P<args>[^)]*)\))?'
        r'(?:[\s\r\n]+@[A-Za-z0-9_().",/ =]+)*'
        r'[\s\r\n]+public\s+[^{(]+\(',
        re.MULTILINE,
    )
    routes: set[str] = set()
    for match in pattern.finditer(text):
        http_method = match.group("method").replace("Mapping", "").upper()
        child = resolve_mapping_arg(match.group("args") or "", constants)
        routes.add(normalize_signature(f"{http_method} {join_paths(base_path, child)}"))
    return routes


def parse_doc_routes(doc_path: Path) -> set[str]:
    text = doc_path.read_text(encoding="utf-8")
    return {
        normalize_signature(match.group(1))
        for match in re.finditer(r'ROUTE:\s*([^\n]+)', text)
    }


errors: list[str] = []
for controller, doc in TARGETS:
    controller_routes = parse_routes(REPO / controller)
    doc_routes = parse_doc_routes(REPO / doc)
    missing = sorted(controller_routes - doc_routes)
    extra = sorted(doc_routes - controller_routes)
    if missing or extra:
        errors.append(f"{controller} <-> {doc}")
        for item in missing:
            errors.append(f"  missing in doc: {item}")
        for item in extra:
            errors.append(f"  extra in doc: {item}")

if errors:
    print("API doc sync check failed:", file=sys.stderr)
    for error in errors:
        print(error, file=sys.stderr)
    sys.exit(1)

print("API doc sync check passed.")
PY
