#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

required_files=(
  "artemis-symphony/skills/new-domain-service.md"
  "artemis-symphony/skills/new-dubbo-client.md"
  "artemis-symphony/skills/add-archunit-rule.md"
  "artemis-symphony/skills/contract-change.md"
  "artemis-symphony/skills/deploy-drill.md"
  "artemis-symphony/skills/expand-existing-service.md"
  "artemis-symphony/skills/spec-driven-delivery.md"
  "artemis-symphony/skills/adversarial-review.md"
  "artemis-symphony/prompts/self-review-and-handoff.md"
  "artemis-symphony/prompts/adversarial-review.md"
  "artemis-symphony/prompts/contract-change-review.md"
  "artemis-symphony/prompts/deploy-drill-report.md"
  "artemis-symphony/prompts/phase-delivery-plan.md"
  "artemis-symphony/prompts/spec-driven-delivery.md"
  "artemis-symphony/tools/README.md"
  "artemis-symphony/tools/registry.json"
)

print_step "Checking Symphony assets"
for file in "${required_files[@]}"; do
  require_repo_path_exact "$file"
done

require_repo_path_exact "artemis-symphony/README.md"
require_repo_path_exact "artemis-symphony/WORKFLOW.md.example"
grep -Fq "contract-change.md" artemis-symphony/README.md
grep -Fq "deploy-drill.md" artemis-symphony/README.md
grep -Fq "expand-existing-service.md" artemis-symphony/README.md
grep -Fq "contract-change.md" artemis-symphony/WORKFLOW.md.example
grep -Fq "deploy-drill.md" artemis-symphony/WORKFLOW.md.example
grep -Fq "phase-delivery-plan.md" artemis-symphony/README.md
grep -Fq "spec-driven-delivery.md" artemis-symphony/README.md
grep -Fq "spec-driven-delivery.md" artemis-symphony/WORKFLOW.md.example
grep -Fq "adversarial-review.md" artemis-symphony/README.md
grep -Fq "adversarial-review.md" artemis-symphony/WORKFLOW.md.example
grep -Fq "artemis-symphony/tools/registry.json" artemis-symphony/README.md
grep -Fq "artemis-symphony/tools/registry.json" artemis-symphony/WORKFLOW.md.example

python3 - <<'PY'
from pathlib import Path
import json
import sys

repo = Path.cwd()
registry_path = repo / "artemis-symphony/tools/registry.json"
executor_path = repo / (
    "artemis-symphony/artemis-symphony-orchestrator/src/main/java/"
    "com/aotemiao/artemis/symphony/orchestrator/LinearGraphqlDynamicToolExecutor.java"
)

errors: list[str] = []
allowed_statuses = {"active", "planned", "deprecated"}
required_output_keys = {"success", "output", "contentItems"}
required_audit_events = {"tool_call_completed", "tool_call_failed"}

try:
    registry = json.loads(registry_path.read_text(encoding="utf-8"))
except json.JSONDecodeError as exc:
    raise SystemExit(f"{registry_path.relative_to(repo)}:{exc.lineno}: invalid JSON ({exc.msg})") from exc


def label(name: str, index: int) -> str:
    return name or f"tools[{index}]"


def is_non_empty_string(value: object) -> bool:
    return isinstance(value, str) and bool(value.strip())


def require_non_empty_string(path: str, value: object) -> None:
    if not is_non_empty_string(value):
        errors.append(f"registry.json: {path} must be a non-empty string")


def require_boolean(path: str, value: object) -> None:
    if not isinstance(value, bool):
        errors.append(f"registry.json: {path} must be boolean")


def require_object_schema(path: str, schema: object) -> dict:
    if not isinstance(schema, dict):
        errors.append(f"registry.json: {path} must be an object")
        return {}
    if schema.get("type") != "object":
        errors.append(f"registry.json: {path}.type must be object")
    return schema


def require_string_list(path: str, value: object, *, non_empty: bool = False) -> list[str]:
    if not isinstance(value, list):
        errors.append(f"registry.json: {path} must be a list")
        return []
    strings: list[str] = []
    for item_index, item in enumerate(value):
        if not is_non_empty_string(item):
            errors.append(f"registry.json: {path}[{item_index}] must be a non-empty string")
        else:
            strings.append(item.strip())
    if non_empty and not strings:
        errors.append(f"registry.json: {path} must contain at least one value")
    return strings


def validate_output_schema(tool_label: str, schema: object) -> None:
    output_schema = require_object_schema(f"tool {tool_label}.output_schema", schema)
    if not output_schema:
        return
    required = set(require_string_list(f"tool {tool_label}.output_schema.required", output_schema.get("required")))
    missing_required = sorted(required_output_keys - required)
    for key in missing_required:
        errors.append(f"registry.json: tool {tool_label}.output_schema.required missing {key}")
    properties = output_schema.get("properties")
    if not isinstance(properties, dict):
        errors.append(f"registry.json: tool {tool_label}.output_schema.properties must be an object")
        return
    expected_types = {
        "success": "boolean",
        "output": "string",
        "contentItems": "array",
    }
    for key, expected_type in expected_types.items():
        property_schema = properties.get(key)
        if not isinstance(property_schema, dict):
            errors.append(f"registry.json: tool {tool_label}.output_schema.properties.{key} must be an object")
            continue
        if property_schema.get("type") != expected_type:
            errors.append(
                f"registry.json: tool {tool_label}.output_schema.properties.{key}.type must be {expected_type}"
            )


def validate_audit(tool_label: str, audit: object) -> None:
    if not isinstance(audit, dict):
        errors.append(f"registry.json: tool {tool_label}.audit must be an object")
        return
    events = set(require_string_list(f"tool {tool_label}.audit.run_history_events", audit.get("run_history_events")))
    for event in sorted(required_audit_events - events):
        errors.append(f"registry.json: tool {tool_label}.audit.run_history_events missing {event}")
    require_non_empty_string(f"tool {tool_label}.audit.external_effect_type", audit.get("external_effect_type"))
    require_non_empty_string(f"tool {tool_label}.audit.low_sensitive_summary", audit.get("low_sensitive_summary"))


def validate_failure_behavior(tool_label: str, failure: object) -> list[str]:
    if not isinstance(failure, dict):
        errors.append(f"registry.json: tool {tool_label}.failure_behavior must be an object")
        return []
    require_boolean(f"tool {tool_label}.failure_behavior.retryable", failure.get("retryable"))
    require_boolean(f"tool {tool_label}.failure_behavior.turn_failure_on_error", failure.get("turn_failure_on_error"))
    codes = require_string_list(
        f"tool {tool_label}.failure_behavior.stable_error_codes",
        failure.get("stable_error_codes"),
        non_empty=True,
    )
    seen_codes: set[str] = set()
    for code in codes:
        if code in seen_codes:
            errors.append(f"registry.json: tool {tool_label}.failure_behavior.stable_error_codes duplicates {code}")
        seen_codes.add(code)
    return codes

if registry.get("schema_version") != 1:
    errors.append("registry.json: schema_version must be 1")
if registry.get("registry_type") != "symphony_tool_registry":
    errors.append("registry.json: registry_type must be symphony_tool_registry")

tools = registry.get("tools")
if not isinstance(tools, list) or not tools:
    errors.append("registry.json: tools must be a non-empty list")
    tools = []

seen: set[str] = set()
required_tool_keys = {
    "name",
    "status",
    "provider",
    "availability",
    "description",
    "input_schema",
    "output_schema",
    "permissions",
    "audit",
    "failure_behavior",
}
required_permission_keys = {
    "permission_level",
    "external_write_allowed",
    "unattended_allowed",
    "requires_human_approval",
    "secrets_access",
}
for index, tool in enumerate(tools):
    if not isinstance(tool, dict):
        errors.append(f"registry.json: tools[{index}] must be an object")
        continue
    name = str(tool.get("name") or "").strip()
    tool_label = label(name, index)
    if not name:
        errors.append(f"registry.json: tools[{index}].name must be non-empty")
    elif name in seen:
        errors.append(f"registry.json: duplicate tool name {name}")
    seen.add(name)
    for key in sorted(required_tool_keys - set(tool.keys())):
        errors.append(f"registry.json: tool {tool_label} missing key {key}")
    if tool.get("status") not in allowed_statuses:
        errors.append(
            f"registry.json: tool {tool_label}.status must be one of {', '.join(sorted(allowed_statuses))}"
        )
    require_non_empty_string(f"tool {tool_label}.provider", tool.get("provider"))
    require_non_empty_string(f"tool {tool_label}.description", tool.get("description"))
    availability = tool.get("availability")
    if not isinstance(availability, dict):
        errors.append(f"registry.json: tool {tool_label}.availability must be an object")
    else:
        require_non_empty_string(f"tool {tool_label}.availability.tracker_kind", availability.get("tracker_kind"))
        require_non_empty_string(f"tool {tool_label}.availability.worker_scope", availability.get("worker_scope"))
    require_object_schema(f"tool {tool_label}.input_schema", tool.get("input_schema"))
    validate_output_schema(tool_label, tool.get("output_schema"))
    validate_audit(tool_label, tool.get("audit"))
    validate_failure_behavior(tool_label, tool.get("failure_behavior"))
    permissions = tool.get("permissions")
    if not isinstance(permissions, dict):
        errors.append(f"registry.json: tool {tool_label}.permissions must be an object")
    else:
        for key in sorted(required_permission_keys - set(permissions.keys())):
            errors.append(f"registry.json: tool {tool_label}.permissions missing key {key}")
        require_non_empty_string(f"tool {tool_label}.permissions.permission_level", permissions.get("permission_level"))
        require_non_empty_string(f"tool {tool_label}.permissions.secrets_access", permissions.get("secrets_access"))
        for key in ("external_write_allowed", "unattended_allowed", "requires_human_approval"):
            if key in permissions and not isinstance(permissions[key], bool):
                errors.append(f"registry.json: tool {tool_label}.permissions.{key} must be boolean")

linear_tools = [tool for tool in tools if isinstance(tool, dict) and tool.get("name") == "linear_graphql"]
if len(linear_tools) != 1:
    errors.append("registry.json: exactly one linear_graphql tool entry is required")
else:
    tool = linear_tools[0]
    if tool.get("provider") != "linear":
        errors.append("registry.json: linear_graphql.provider must be linear")
    availability = tool.get("availability")
    if not isinstance(availability, dict):
        errors.append("registry.json: linear_graphql.availability must be an object")
    elif availability.get("tracker_kind") != "linear":
        errors.append("registry.json: linear_graphql.availability.tracker_kind must be linear")
    input_schema = tool.get("input_schema")
    if isinstance(input_schema, dict):
        required = input_schema.get("required")
        if not isinstance(required, list) or "query" not in required:
            errors.append("registry.json: linear_graphql.input_schema.required must include query")
        properties = input_schema.get("properties")
        if not isinstance(properties, dict) or "query" not in properties or "variables" not in properties:
            errors.append("registry.json: linear_graphql.input_schema.properties must include query and variables")
        else:
            query_schema = properties.get("query")
            variables_schema = properties.get("variables")
            if not isinstance(query_schema, dict) or query_schema.get("type") != "string":
                errors.append("registry.json: linear_graphql.input_schema.properties.query.type must be string")
            if not isinstance(variables_schema, dict) or variables_schema.get("type") != ["object", "null"]:
                errors.append(
                    "registry.json: linear_graphql.input_schema.properties.variables.type must be [object, null]"
                )
    permissions = tool.get("permissions")
    if isinstance(permissions, dict):
        if permissions.get("permission_level") != "external_api":
            errors.append("registry.json: linear_graphql.permissions.permission_level must be external_api")
        if permissions.get("external_write_allowed") is not True:
            errors.append("registry.json: linear_graphql must declare external_write_allowed=true")
    failure = tool.get("failure_behavior")
    if isinstance(failure, dict) and isinstance(failure.get("stable_error_codes"), list):
        linear_error_codes = failure["stable_error_codes"]
        for code in (
            "missing_query",
            "invalid_arguments",
            "invalid_variables",
            "linear_api_status",
            "linear_api_request",
            "tracker_graphql_unsupported",
        ):
            if code not in linear_error_codes:
                errors.append(f"registry.json: linear_graphql missing stable error code {code}")

executor_text = executor_path.read_text(encoding="utf-8")
if '"linear_graphql"' not in executor_text:
    errors.append("LinearGraphqlDynamicToolExecutor.java: runtime tool name linear_graphql not found")
if "TOOL_INPUT_SCHEMA" not in executor_text:
    errors.append("LinearGraphqlDynamicToolExecutor.java: TOOL_INPUT_SCHEMA not found")
if linear_tools:
    for code in linear_tools[0].get("failure_behavior", {}).get("stable_error_codes", []):
        if isinstance(code, str) and f'"{code}"' not in executor_text:
            errors.append(f"LinearGraphqlDynamicToolExecutor.java: stable error code {code} not found in runtime")

if errors:
    print("Symphony tool registry check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    raise SystemExit(1)

print("Symphony tool registry check passed.")
PY

print_step "Symphony asset check passed"
