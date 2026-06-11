#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Checking harness metrics report generator"

require_repo_path_exact "scripts/harness/generate-harness-metrics-report.sh"
require_repo_path_exact "docs/reports/harness-metrics/README.md"

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

eval_dir="$tmp_dir/agent-evals/eval-1"
suite_dir="$tmp_dir/agent-evals/suite-1"
suite_case_dir="$suite_dir/cases/eval-2"
run_dir="$tmp_dir/agent-runs"
delivery_dir="$tmp_dir/harness-delivery-signals"
deploy_dir="$tmp_dir/deploy-drills"
drafts_dir="$tmp_dir/agent-eval-drafts"
out_dir="$tmp_dir/harness-metrics"
mkdir -p "$eval_dir" "$suite_case_dir/agent-runs" "$run_dir" "$delivery_dir" "$deploy_dir" "$drafts_dir"

cat >"$eval_dir/summary.json" <<'JSON'
{
  "eval_id": "fixture-eval",
  "status": "passed",
  "total_tokens": 42,
  "metrics": {
    "total_runs": 1,
    "completed_runs": 1,
    "total_tokens": 42,
    "status_counts": {
      "completed": 1
    }
  }
}
JSON

cat >"$suite_dir/summary.json" <<'JSON'
{
  "schema_version": 1,
  "summary_type": "symphony_memory_eval_suite",
  "status": "passed",
  "total": 1,
  "passed": 1,
  "failed": 0
}
JSON

cat >"$suite_case_dir/summary.json" <<'JSON'
{
  "eval_id": "fixture-suite-case",
  "status": "passed",
  "total_tokens": 21
}
JSON

cat >"$run_dir/run-1.json" <<'JSON'
{
  "schema_version": 1,
  "summary_type": "symphony_agent_run",
  "run_id": "run-1",
  "status": "failed",
  "failure_reason": "permission preflight failed: writable root outside",
  "failure_category": "permission",
  "duration_seconds": 12,
  "attempt": {
    "turn_count": 2
  },
  "workspace": {
    "artifact_inventory": {
      "file_count": 1,
      "total_bytes": 12,
      "truncated": false,
      "scan_error": "",
      "files": [
        {
          "path": "EVAL_RESULT.md",
          "size_bytes": 12
        }
      ]
    }
  },
  "codex": {
    "event_counts": {
      "session_started": 1,
      "thread/tokenUsage/updated": 1,
      "turn_failed": 1
    },
    "usage": {
      "total_tokens": 42
    }
  },
  "environment": {
    "java": {
      "version": "21.0.8",
      "vendor": "Fixture JDK"
    },
    "os": {
      "name": "FixtureOS",
      "arch": "arm64"
    },
    "spring_profiles": ["eval"]
  },
  "permissions": {
    "approval_policy": {
      "reject": {
        "sandbox_approval": true,
        "rules": true,
        "mcp_elicitations": true
      }
    },
    "thread_sandbox": "workspace-write",
    "turn_sandbox_policy": {
      "type": "workspaceWrite",
      "writableRoots": ["workspace/run-1"]
    },
    "remote_worker": false,
    "network_access": false,
    "writable_roots": ["workspace/run-1"],
    "allowed_writable_roots": ["configured-writable-root/1-cache"],
    "danger_full_access_allowed": false
  },
  "retry": {
    "scheduled": true,
    "dispatch_kind": "retry"
  },
  "external_effects": {
    "tracker_state_claimed": true,
    "linear_comment_attempted": true,
    "events": [
      {
        "type": "tracker_state_update",
        "provider": "memory",
        "target": "issue-1",
        "status": "succeeded",
        "error_code": "",
        "error_message": "",
        "at": "2026-06-10T00:00:00Z"
      },
      {
        "type": "linear_comment",
        "provider": "memory",
        "target": "issue-1",
        "status": "failed",
        "error_code": "memory_comment_body",
        "error_message": "body is required",
        "at": "2026-06-10T00:00:01Z"
      }
    ]
  }
}
JSON

cat >"$suite_case_dir/agent-runs/run-2.json" <<'JSON'
{
  "schema_version": 1,
  "summary_type": "symphony_agent_run",
  "run_id": "run-2",
  "status": "completed",
  "duration_seconds": 4,
  "attempt": {
    "turn_count": 1
  },
  "workspace": {
    "artifact_inventory": {
      "file_count": 2,
      "total_bytes": 21,
      "truncated": true,
      "scan_error": "",
      "files": []
    }
  },
  "codex": {
    "event_counts": {
      "session_started": 1,
      "thread/tokenUsage/updated": 1,
      "turn_completed": 1
    },
    "usage": {
      "total_tokens": 21
    }
  },
  "environment": {
    "java": {
      "version": "21.0.8",
      "vendor": "Fixture JDK"
    },
    "os": {
      "name": "FixtureOS",
      "arch": "arm64"
    },
    "spring_profiles": []
  },
  "permissions": {
    "approval_policy": "on-request",
    "thread_sandbox": "workspace-write",
    "turn_sandbox_policy": {
      "type": "workspaceWrite",
      "writableRoots": ["workspace/run-2", "configured-writable-root/1-cache"]
    },
    "remote_worker": false,
    "network_access": true,
    "writable_roots": ["workspace/run-2", "configured-writable-root/1-cache"],
    "allowed_writable_roots": [],
    "danger_full_access_allowed": false
  },
  "retry": {
    "scheduled": false,
    "dispatch_kind": ""
  },
  "external_effects": {
    "tracker_state_claimed": true,
    "linear_comment_attempted": false,
    "events": [
      {
        "type": "tracker_state_update",
        "provider": "memory",
        "target": "issue-2",
        "status": "succeeded",
        "error_code": "",
        "error_message": "",
        "at": "2026-06-10T00:00:02Z"
      }
    ]
  }
}
JSON

cat >"$suite_case_dir/agent-runs/run-3.json" <<'JSON'
{
  "schema_version": 1,
  "summary_type": "symphony_agent_run",
  "run_id": "run-3",
  "status": "completed",
  "duration_seconds": 2,
  "attempt": {
    "turn_count": 1
  },
  "workspace": {
    "artifact_inventory": {
      "file_count": 0,
      "total_bytes": 0,
      "truncated": false,
      "scan_error": "remote_workspace_not_scanned",
      "files": []
    }
  },
  "codex": {
    "event_counts": {
      "session_started": 1,
      "turn_completed": 1
    },
    "usage": {
      "total_tokens": 7
    }
  },
  "environment": {
    "java": {
      "version": "17.0.10",
      "vendor": "Fixture JDK"
    },
    "os": {
      "name": "OtherOS",
      "arch": "x86_64"
    },
    "spring_profiles": ["ci"]
  },
  "permissions": {
    "approval_policy": "never",
    "thread_sandbox": "danger-full-access",
    "turn_sandbox_policy": {
      "type": "readOnly",
      "writableRoots": []
    },
    "remote_worker": true,
    "network_access": false,
    "writable_roots": [],
    "allowed_writable_roots": [],
    "danger_full_access_allowed": true
  },
  "retry": {
    "scheduled": true,
    "dispatch_kind": "continuation"
  },
  "external_effects": {
    "tracker_state_claimed": false,
    "linear_comment_attempted": false,
    "events": []
  }
}
JSON

cat >"$delivery_dir/github-pr-review.json" <<'JSON'
{
  "schema_version": 1,
  "summary_type": "harness_delivery_signal",
  "provider": "github",
  "pull_requests": {
    "created": 2,
    "merged": 1,
    "reverted": 1,
    "merge_time_seconds": [120, 240]
  },
  "review_findings": {
    "total": 3,
    "severity_counts": {
      "p1": 1,
      "p2": 2
    },
    "category_counts": {
      "bug": 2,
      "security": 1
    }
  }
}
JSON

cat >"$deploy_dir/2026-06-10-system.md" <<'MD'
# Deploy Drill Report

Status: completed
Last Reviewed: 2026-06-10
Review Cadence: 90 days

## 演练范围

- 服务：system

## 执行命令

```bash
scripts/dev/deploy-drill.sh system
```

## 验证结果

| 检查项 | 命令或证据 | 结果 |
|--------|------------|------|
| smoke | `scripts/smoke/system-lookup.sh` | 通过 |

## 指标摘要

```json
{
  "schema_version": 1,
  "summary_type": "deploy_drill_report",
  "kind": "deploy",
  "service": "system",
  "services": ["system"],
  "status": "completed",
  "smoke": "passed",
  "rollback": false,
  "failure_stage": ""
}
```

## 结论

- 通过。
MD

cat >"$deploy_dir/2026-06-10-gateway-rollback.md" <<'MD'
# Rollback Drill Report

Status: completed
Last Reviewed: 2026-06-10
Review Cadence: 90 days

## 演练范围

- 服务：gateway

## 执行命令

```bash
scripts/dev/rollback-drill.sh gateway gateway:previous
```

## 验证结果

| 检查项 | 命令或证据 | 结果 |
|--------|------------|------|
| 回滚目标存在 | gateway:previous | 通过 |

## 指标摘要

```json
{
  "schema_version": 1,
  "summary_type": "deploy_drill_report",
  "kind": "rollback",
  "service": "gateway",
  "services": ["gateway"],
  "status": "completed",
  "smoke": "pending",
  "rollback": true,
  "failure_stage": ""
}
```

## 结论

- 通过。
MD

cat >"$deploy_dir/2026-06-10-sample-template.md" <<'MD'
# Deploy Drill Report Template

Status: template
Last Reviewed: 2026-06-10
Review Cadence: 90 days

## 演练范围

- 服务：template

## 执行命令

```bash
scripts/dev/deploy-drill.sh <service>
```

## 验证结果

| 检查项 | 命令或证据 | 结果 |
|--------|------------|------|
| smoke | `scripts/smoke/<name>.sh` | 待填写 |

## 指标摘要

```json
{
  "schema_version": 1,
  "summary_type": "deploy_drill_report",
  "kind": "deploy",
  "service": "template",
  "services": ["template"],
  "status": "planned",
  "smoke": "pending",
  "rollback": false,
  "failure_stage": ""
}
```

## 结论

- 模板不应进入指标。
MD

cat >"$drafts_dir/memory-regression-fixture-permission.yml" <<'YAML'
# Generated from a low-sensitive symphony_agent_run summary.
# Manual review is required before moving this draft into docs/agent-evals/datasets/.
id: memory-regression-fixture-permission
title: Memory regression draft for permission
risk_level: high
tracker: memory
issue_id: eval-memory-regression-fixture-permission
issue_identifier: EVAL-DRAFT
issue_title: Permission regression draft
manual_review_required: true
expected_workspace_file: EVAL_RESULT.md
expected_workspace_contains: should not be written
expected_workspace_present: false
expected_codex_started: false
expected_summary_status: failed
expected_history_status: failed
expected_failure_reason_contains: permission preflight failed
expected_failure_category: permission
expected_events:
  - run_started
  - run_failed
required_validations:
  - scripts/e2e/run-symphony-agent-eval.sh
YAML

collector_event_path="$tmp_dir/github-pr-closed-event.json"
collector_output_dir="$tmp_dir/collector-delivery-signals"
collector_output_path="$collector_output_dir/github-event.json"
mkdir -p "$collector_output_dir"
cat >"$collector_event_path" <<'JSON'
{
  "action": "closed",
  "pull_request": {
    "merged": true,
    "created_at": "2026-06-10T00:00:00Z",
    "merged_at": "2026-06-10T00:05:00Z"
  }
}
JSON

GITHUB_EVENT_NAME="pull_request" \
  GITHUB_REPOSITORY="example/repo" \
  GITHUB_RUN_ID="12345" \
  scripts/harness/collect-github-delivery-signal.sh "$collector_event_path" "$collector_output_path" >/dev/null

python3 - "$collector_output_path" <<'PY'
from pathlib import Path
import json
import sys

body = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
assert body["summary_type"] == "harness_delivery_signal"
assert body["provider"] == "github"
assert body["source"]["event_name"] == "pull_request"
assert body["source"]["action"] == "closed"
assert body["pull_requests"]["created"] == 0
assert body["pull_requests"]["merged"] == 1
assert body["pull_requests"]["reverted"] == 0
assert body["pull_requests"]["merge_time_seconds"] == [300]
assert body["review_findings"]["total"] == 0
PY

scripts/harness/generate-harness-metrics-report.sh \
  "$tmp_dir/agent-evals" \
  "$run_dir" \
  "$out_dir" \
  "$delivery_dir" \
  "$deploy_dir" \
  "$drafts_dir" >/dev/null

python3 - "$out_dir" <<'PY'
from pathlib import Path
import json
import sys

out_dir = Path(sys.argv[1])
json_path = out_dir / "latest.json"
md_path = out_dir / "latest.md"
if not json_path.exists() or not md_path.exists():
    raise SystemExit("metrics generator did not create latest.json and latest.md")

body = json.loads(json_path.read_text(encoding="utf-8"))
assert body["summary_type"] == "harness_metrics_snapshot"
assert body["evals"]["total"] == 2
assert body["evals"]["passed"] == 2
assert body["evals"]["pass_rate"] == 100.0
assert body["eval_suites"]["total"] == 1
assert body["eval_suites"]["passed"] == 1
assert body["eval_suites"]["case_total"] == 1
assert body["eval_suites"]["case_passed"] == 1
assert body["eval_suites"]["case_failed"] == 0
assert body["agent_runs"]["total"] == 3
assert body["agent_runs"]["completed"] == 2
assert body["agent_runs"]["success_rate"] == 66.67
assert body["agent_runs"]["retried"] == 1
assert body["agent_runs"]["failure_categories"] == {"permission": 1}
assert body["agent_runs"]["average_duration_seconds"] == 6.0
assert body["agent_runs"]["average_turns"] == 1.33
assert body["agent_runs"]["total_tokens"] == 70
assert body["agent_runs"]["codex_events"] == {
    "total": 8,
    "event_counts": {
        "session_started": 3,
        "thread/tokenUsage/updated": 2,
        "turn_completed": 2,
        "turn_failed": 1,
    },
}
assert body["agent_runs"]["external_effects"] == {
    "tracker_state_claimed": 2,
    "linear_comment_attempted": 1,
}
assert body["agent_runs"]["external_effect_events"] == {
    "total": 3,
    "status_counts": {"failed": 1, "succeeded": 2},
    "type_counts": {"linear_comment": 1, "tracker_state_update": 2},
    "type_status_counts": {"linear_comment:failed": 1, "tracker_state_update:succeeded": 2},
    "error_code_counts": {"memory_comment_body": 1},
}
assert body["agent_runs"]["environment"] == {
    "java_major_counts": {"17": 1, "21": 2},
    "os_name_counts": {"fixtureos": 2, "otheros": 1},
    "os_arch_counts": {"arm64": 2, "x86_64": 1},
    "spring_profile_counts": {"ci": 1, "default": 1, "eval": 1},
}
assert body["agent_runs"]["permission_posture"] == {
    "summaries_with_permissions": 3,
    "remote_worker_runs": 1,
    "network_access_runs": 1,
    "danger_full_access_allowed_runs": 1,
    "approval_policy_counts": {"never": 1, "on-request": 1, "reject": 1},
    "thread_sandbox_counts": {"danger-full-access": 1, "workspace-write": 2},
    "turn_sandbox_type_counts": {"readonly": 1, "workspacewrite": 2},
    "writable_root_count_distribution": {"0": 1, "1": 1, "2": 1},
    "allowed_writable_root_count_distribution": {"0": 2, "1": 1},
}
assert body["agent_runs"]["workspace_artifacts"] == {
    "total_files": 3,
    "average_files": 1.0,
    "total_bytes": 33,
    "average_bytes": 11.0,
    "truncated_runs": 1,
    "scan_error_counts": {"remote_workspace_not_scanned": 1},
}
assert body["delivery"] == {
    "total_documents": 1,
    "provider_counts": {"github": 1},
    "pull_requests": {
        "created": 2,
        "merged": 1,
        "reverted": 1,
        "average_merge_time_seconds": 180.0,
    },
    "review_findings": {
        "total": 3,
        "severity_counts": {"p1": 1, "p2": 2},
        "category_counts": {"bug": 2, "security": 1},
    },
}
assert body["deploy_drills"] == {
    "total_reports": 2,
    "kind_counts": {"deploy": 1, "rollback": 1},
    "status_counts": {"completed": 2},
    "service_counts": {"gateway": 1, "system": 1},
    "smoke_counts": {"passed": 1, "pending": 1},
    "failure_stage_counts": {},
}
assert body["agent_eval_drafts"] == {
    "total": 1,
    "manual_review_required": 1,
    "failure_category_counts": {"permission": 1},
    "risk_level_counts": {"high": 1},
}

text = md_path.read_text(encoding="utf-8")
for marker in (
    "# Harness Metrics Snapshot",
    "## Agent Eval 指标",
    "## Agent Eval Suite 指标",
    "## Agent Run 指标",
    "## Codex 事件",
    "## 外部副作用",
    "## 运行环境",
    "## 权限姿态",
    "## Workspace Artifact Inventory",
    "## Delivery 信号",
    "## 部署 / 回滚演练",
    "## Agent Eval 草稿",
    "Java major",
    "drill smoke",
    "draft failure category",
    "review finding severity",
    "Codex event counts",
    "external effect error code",
    "turn sandbox type",
    "writable root counts",
):
    if marker not in text:
        raise SystemExit(f"missing markdown marker: {marker}")
PY

ci_out_dir="$tmp_dir/ci-harness-metrics"
HARNESS_METRICS_EVALS_DIR="$tmp_dir/agent-evals" \
  HARNESS_METRICS_AGENT_RUNS_DIR="$run_dir" \
  HARNESS_METRICS_DELIVERY_SIGNALS_DIR="$delivery_dir" \
  HARNESS_METRICS_DEPLOY_DRILLS_DIR="$deploy_dir" \
  HARNESS_METRICS_AGENT_EVAL_DRAFTS_DIR="$drafts_dir" \
  scripts/harness/generate-ci-harness-metrics.sh "$ci_out_dir" >/dev/null

python3 - "$ci_out_dir" <<'PY'
from pathlib import Path
import json
import sys

out_dir = Path(sys.argv[1])
json_path = out_dir / "latest.json"
md_path = out_dir / "latest.md"
if not json_path.exists() or not md_path.exists():
    raise SystemExit("CI metrics wrapper did not create latest.json and latest.md")

body = json.loads(json_path.read_text(encoding="utf-8"))
assert body["summary_type"] == "harness_metrics_snapshot"
assert body["agent_runs"]["total"] == 3
assert body["agent_runs"]["codex_events"]["event_counts"]["turn_completed"] == 2
assert body["agent_runs"]["external_effect_events"]["error_code_counts"] == {"memory_comment_body": 1}
assert body["agent_runs"]["environment"]["java_major_counts"] == {"17": 1, "21": 2}
assert body["agent_runs"]["permission_posture"]["network_access_runs"] == 1
assert body["agent_runs"]["permission_posture"]["thread_sandbox_counts"] == {
    "danger-full-access": 1,
    "workspace-write": 2,
}
assert body["agent_runs"]["workspace_artifacts"]["total_files"] == 3
assert body["delivery"]["review_findings"]["severity_counts"] == {"p1": 1, "p2": 2}
assert body["deploy_drills"]["kind_counts"] == {"deploy": 1, "rollback": 1}
assert body["agent_eval_drafts"]["failure_category_counts"] == {"permission": 1}
PY

print_step "Harness metrics report check passed"
