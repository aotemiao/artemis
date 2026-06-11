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
TARGETS: list[tuple[Path, Path]] = [
    (
        Path("artemis-auth/src/main/java/com/aotemiao/artemis/auth/web/AuthController.java"),
        Path("artemis-auth/AUTH_API.md"),
    ),
]
SYSTEM_SERVICE_ROOT = Path("artemis-modules/artemis-system")
SYSTEM_CONTROLLER_ROOT = (
    SYSTEM_SERVICE_ROOT
    / "artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web"
)
SYSTEM_DOC_NAME_OVERRIDES = {
    # 字典能力沿用既有 LOOKUP_API.md，不按类名推导成 LOOKUP_TYPE_API.md。
    "LookupTypeController": "LOOKUP_API.md",
}


def camel_to_api_doc_name(controller_name: str) -> str:
    stem = controller_name.removesuffix("Controller")
    stem = stem.removeprefix("System")
    words = re.findall(r"[A-Z]+(?=[A-Z][a-z]|\d|\b)|[A-Z]?[a-z]+|\d+", stem)
    if not words:
        raise ValueError(f"Unable to infer API doc name from controller: {controller_name}")
    return f"{'_'.join(word.upper() for word in words)}_API.md"


def system_api_doc_for(controller: Path) -> Path:
    controller_name = controller.stem
    doc_name = SYSTEM_DOC_NAME_OVERRIDES.get(controller_name, camel_to_api_doc_name(controller_name))
    return SYSTEM_SERVICE_ROOT / doc_name


def exact_exists(path: Path) -> bool:
    try:
        rel = path.resolve().relative_to(REPO.resolve())
    except ValueError:
        return path.exists()

    current = REPO.resolve()
    for part in rel.parts:
        try:
            names = {child.name for child in current.iterdir()}
        except FileNotFoundError:
            return False
        if part not in names:
            return False
        current = current / part
    return True

for controller_candidate in sorted((REPO / SYSTEM_CONTROLLER_ROOT).glob("**/*Controller.java")):
    controller = controller_candidate.relative_to(REPO)
    TARGETS.append((controller, system_api_doc_for(controller)))

for service_doc in sorted(REPO.glob("artemis-modules/artemis-*/SERVICE_API.md")):
    service_root = service_doc.parent
    controller_candidates = sorted(service_root.glob("artemis-*-adapter/src/main/java/**/adapter/web/**/*Controller.java"))
    if not controller_candidates:
        continue
    for controller_candidate in controller_candidates:
        TARGETS.append((controller_candidate.relative_to(REPO), service_doc.relative_to(REPO)))


def normalize_signature(value: str) -> str:
    return " ".join(value.strip().strip("`").split())


def resolve_mapping_arg(raw: str, constants: dict[str, str]) -> str:
    value = raw.strip()
    if not value:
        return ""
    named_mapping = re.search(r'(?:value|path)\s*=\s*([^,]+)', value)
    if named_mapping:
        value = named_mapping.group(1).strip()
    elif "=" in value:
        _, value = value.split("=", 1)
        value = value.split(",", 1)[0].strip()
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
targets_by_doc: dict[Path, list[Path]] = {}
for controller, doc in TARGETS:
    targets_by_doc.setdefault(doc, []).append(controller)

for doc, controllers in targets_by_doc.items():
    controller_routes: set[str] = set()
    missing_controller_paths: list[Path] = []
    for controller in controllers:
        if not exact_exists(REPO / controller):
            missing_controller_paths.append(controller)
            continue
        controller_routes.update(parse_routes(REPO / controller))
    if missing_controller_paths:
        for controller in missing_controller_paths:
            errors.append(f"missing controller path with exact casing: {controller}")
        continue
    if not exact_exists(REPO / doc):
        errors.append(f"missing API doc path with exact casing: {doc}")
        continue
    doc_routes = parse_doc_routes(REPO / doc)
    missing = sorted(controller_routes - doc_routes)
    extra = sorted(doc_routes - controller_routes)
    if missing or extra:
        controller_label = ", ".join(str(controller) for controller in controllers)
        errors.append(f"{controller_label} <-> {doc}")
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
