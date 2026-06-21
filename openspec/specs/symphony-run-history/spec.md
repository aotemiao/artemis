## ADDED Requirements

### Requirement: Symphony SHALL persist local run history in SQLite

`artemis-symphony` SHALL provide a local SQLite-backed run history store for operator visibility. The store SHALL record at minimum:

- issue id and identifier
- tracker state
- worker host and workspace path
- run status
- attempt number
- Codex thread/session identifiers when available
- token totals when available
- failure reason and stable failure category when available
- started, updated, and finished timestamps

SQLite persistence SHALL be best-effort for the orchestration path: a persistence failure MUST be logged but MUST NOT by itself fail an active worker attempt.

The SQLite database path SHALL default to `./symphony_runs.sqlite` and SHALL be configurable through Spring application configuration.

#### Scenario: Worker attempt starts

- **WHEN** Symphony dispatches an issue to a worker
- **THEN** a run history record SHALL be created or updated in the local SQLite database
- **AND** the event stream for that run SHALL include one `run_started` event

#### Scenario: Worker attempt start is recorded more than once

- **WHEN** the same run is updated again during issue claim or runtime metadata refresh
- **THEN** the run history event stream SHALL NOT contain duplicate `run_started` events for that run

#### Scenario: Worker attempt receives Codex events

- **WHEN** Symphony receives Codex app-server updates for a running issue
- **THEN** the event SHALL be appended to the run history store with bounded payload content suitable for local troubleshooting

#### Scenario: before_run hook fails before Codex startup

- **WHEN** the issue workspace is created and the configured `before_run` hook fails
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain `run_started` and `run_failed`
- **AND** the event stream SHALL NOT include session, turn, or token usage events for that failed attempt
- **AND** the workspace success marker for executable evals SHALL NOT be written
- **AND** the failure reason SHALL identify the before_run hook failure

#### Scenario: after_create hook fails before permission preflight

- **WHEN** Symphony creates a new issue workspace and the configured `after_create` hook fails
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain `run_started` and `run_failed`
- **AND** the event stream SHALL NOT include session, turn, or token usage events for that failed attempt
- **AND** the workspace success marker for executable evals SHALL NOT be written
- **AND** the failure reason SHALL identify the after_create hook failure

#### Scenario: Codex turn fails after startup

- **WHEN** Codex app-server starts and a turn later fails
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain the session start, token usage when available, turn failure, and run failure events
- **AND** the failure reason SHALL identify the Codex turn failure

#### Scenario: Codex turn is cancelled after startup

- **WHEN** Codex app-server starts and a turn is later cancelled
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain the session start, token usage when available, `turn_cancelled`, and run failure events
- **AND** the workspace success marker for executable evals SHALL NOT be written

#### Scenario: Codex turn times out after startup

- **WHEN** Codex app-server starts and a turn does not produce a terminal turn event before `codex.turn_timeout_ms`
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain the session start, token usage when available, `turn_ended_with_error`, and run failure events
- **AND** the workspace success marker for executable evals SHALL NOT be written
- **AND** the failure reason SHALL identify the Codex turn timeout

#### Scenario: Codex turn start response times out after startup

- **WHEN** Codex app-server starts but does not respond to the `turn/start` request before `codex.read_timeout_ms`
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain the session start, `turn_ended_with_error`, and run failure events
- **AND** the event stream SHALL NOT include token usage or terminal turn events for that failed attempt
- **AND** the workspace success marker for executable evals SHALL NOT be written
- **AND** the failure reason SHALL identify the Codex turn response timeout

#### Scenario: Codex emits malformed stdout during a successful turn

- **WHEN** Codex app-server emits a malformed stdout line and later completes the turn successfully
- **THEN** the run history record SHALL be finished with status `completed`
- **AND** the event stream SHALL retain `malformed`, token usage when available, and `turn_completed`
- **AND** the malformed payload SHALL be bounded for local troubleshooting
- **AND** the malformed stdout line by itself SHALL NOT fail the worker attempt

#### Scenario: Codex turn requests approval when auto-approval is disabled

- **WHEN** Codex app-server starts and later requests command or file-change approval while the effective approval policy is not `never`
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain the session start, token usage when available, `approval_required`, `turn_ended_with_error`, and run failure events
- **AND** the event stream SHALL NOT include `approval_auto_approved`
- **AND** the failure reason SHALL identify that Codex approval is required

#### Scenario: Codex dynamic tool call fails after startup

- **WHEN** Codex app-server starts and a dynamic tool call returns `success: false`
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain the session start, token usage when available, `tool_call_failed`, `turn_ended_with_error`, and run failure events
- **AND** the workspace success marker for executable evals SHALL NOT be written
- **AND** the failure reason SHALL identify the dynamic tool failure

#### Scenario: Codex requires unsupported interactive user input

- **WHEN** Codex app-server starts and later requests user input that cannot be answered in the non-interactive worker path
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain the session start, token usage when available, `turn_input_required`, `turn_ended_with_error`, and run failure events
- **AND** the event stream SHALL NOT include `tool_input_auto_answered`
- **AND** the workspace success marker for executable evals SHALL NOT be written
- **AND** the failure reason SHALL identify that user input is required

#### Scenario: Codex app-server startup handshake fails

- **WHEN** Codex app-server process starts but the startup handshake fails
- **THEN** the run history record SHALL be finished with status `failed`
- **AND** the event stream SHALL retain `startup_failed` and `run_failed`
- **AND** the event stream SHALL NOT include session, turn, or token usage events for that failed attempt
- **AND** the failure reason SHALL identify the startup failure

#### Scenario: SQLite write fails

- **WHEN** the local SQLite database cannot be written
- **THEN** Symphony SHALL log the persistence failure and continue the worker lifecycle when possible

#### Scenario: Reconciliation terminates a running attempt

- **WHEN** Symphony stops a running attempt because tracker reconciliation marks it terminal, unrouted, non-active, missing, or stalled
- **THEN** the run history record SHALL be finished with status `terminated`
- **AND** the failure reason SHALL explain the reconciliation cause

#### Scenario: Startup recovers stale running attempts

- **WHEN** Symphony starts and the local run history store contains unfinished records with status `running`
- **THEN** those records SHALL be finished with status `interrupted`
- **AND** a `run_interrupted` event SHALL be appended for each recovered record

#### Scenario: Operator configures database path

- **WHEN** Symphony starts with `symphony.history.sqlite-path`
- **THEN** run history SHALL be stored at the configured SQLite path

### Requirement: Symphony SHALL expose run history through HTTP APIs

`artemis-symphony-start` SHALL expose read-only HTTP APIs for local run history. At minimum the APIs SHALL support:

- listing recent runs with a bounded limit
- reading events for a specific run id
- summarizing recent runs into bounded operational metrics

#### Scenario: Operator lists recent runs

- **WHEN** an operator requests the run history API
- **THEN** Symphony SHALL return recent run records ordered by latest update time

#### Scenario: Operator reads run events

- **WHEN** an operator requests events for a known run id
- **THEN** Symphony SHALL return recent events for that run in event-time order

#### Scenario: Operator reads run metrics

- **WHEN** an operator requests the run metrics API
- **THEN** Symphony SHALL return a bounded recent-run summary with status counts, stable failure category counts, retry count, token totals, average duration, and the covered time window

### Requirement: Symphony SHALL provide a simple local visualization page

`artemis-symphony-start` SHALL provide a simple browser-accessible visualization page for local operators. The page SHALL render recent run history from the HTTP APIs and link to the existing JSON state surface.

#### Scenario: Operator opens the run history page

- **WHEN** an operator opens the Symphony visualization page
- **THEN** the page SHALL show recent run metrics, status distribution, stable failure category distribution, success rate, average duration, recent runs, issue identifiers, worker information, timestamps, token totals, and a way to inspect run events

### Requirement: Symphony SHALL emit low-sensitivity agent run summaries

`artemis-symphony` SHALL emit a local JSON summary when an agent attempt finishes, fails, or is terminated by reconciliation. The summary SHALL include at minimum:

- run id
- issue id, identifier, title, and tracker state when available
- attempt number and turn count
- status, failure reason, and stable failure category when available
- worker host and workspace path
- bounded workspace artifact inventory, including relative path, observed file count, total bytes, truncation marker, and scan error code when applicable
- Codex session id, last event, bounded event type counts, and token totals when available
- approval policy, thread sandbox, resolved turn sandbox policy, network access, writable roots, and remote-worker marker
- low-sensitivity runtime environment snapshot, including Java runtime, OS, CPU architecture, available processors, Maven version placeholder, and Spring profiles
- retry plan when scheduled, including dispatch kind
- bounded external effect markers and event entries, such as tracker state claim or Linear comment writeback attempt, including type, provider, target, status, timestamp, and bounded error metadata

`retry.scheduled=true` SHALL mean a failed attempt has a failure retry scheduled. A successful attempt MAY schedule a continuation check to determine whether the issue remains active, but that continuation SHALL be represented with a non-retry dispatch kind and SHALL NOT count as a failure retry.

The summary output SHALL be best-effort for the orchestration path: a summary write failure MUST be logged but MUST NOT by itself fail an active worker attempt or its retry scheduling. The default directory SHALL be `artifacts/agent-runs/`, and workflow configuration SHALL allow the operator to disable summary output or set a different directory.

The summary MUST NOT include full prompts, full chat transcripts, full tool outputs, secrets, or raw external system responses.

Repository governance SHALL provide a scriptable sensitive-content check for manually retained summaries under `docs/reports/agent-runs/` and generated low-sensitivity artifact summaries. The JSON summary structure contract SHALL be owned by the writer implementation and its unit test (`AgentRunSummaryWriterTest`), and SHALL NOT be re-validated by the governance script to avoid a duplicated cross-language schema.

#### Scenario: Worker attempt writes summary

- **WHEN** a worker attempt exits normally or fails
- **THEN** Symphony SHALL write one low-sensitivity JSON summary for the attempt when summary output is enabled
- **AND** the summary SHALL include status, failure category, token totals, Codex event counts, workspace, issue, attempt, permissions, environment, and retry fields
- **AND** `codex.event_counts` SHALL contain only event type strings and integer counts, without event payloads, prompts, tool outputs, or stdout content
- **AND** `workspace.artifact_inventory.files` SHALL contain only workspace-relative paths and file sizes, without file content or absolute artifact paths
- **AND** `external_effects.events` SHALL be present as a list, even when no external write occurred

#### Scenario: External tracker write is audited

- **WHEN** Symphony attempts an external tracker state update or Linear comment writeback during an attempt
- **THEN** the agent run summary SHALL include a bounded event entry with effect type, provider, target id, status, timestamp, and error code/message when applicable
- **AND** the event entry SHALL NOT include full request bodies, comment bodies, raw external responses, secrets, or connection strings

#### Scenario: Worker attempt records permission snapshot

- **WHEN** Symphony writes an agent run summary
- **THEN** the summary SHALL include the Codex approval policy and sandbox policy used for the attempt
- **AND** it SHALL expose whether network access was enabled and which writable roots were configured when available

#### Scenario: Worker attempt records low-sensitivity environment snapshot

- **WHEN** Symphony writes an agent run summary
- **THEN** the summary SHALL include an `environment` object with Java runtime, Maven version placeholder, OS name, OS architecture, available processor count, and Spring profiles
- **AND** the environment snapshot SHALL NOT include usernames, home directories, PATH, complete environment variables, Maven local repository paths, secrets, or connection strings

#### Scenario: Reconciliation termination writes summary

- **WHEN** Symphony terminates a running attempt through reconciliation
- **THEN** Symphony SHALL write one low-sensitivity JSON summary with status `terminated`
- **AND** the failure reason SHALL explain the reconciliation cause

#### Scenario: Operator disables summary output

- **WHEN** workflow config sets `reporting.agent_runs.enabled` to `false`
- **THEN** Symphony SHALL NOT write agent run summary files

### Requirement: Symphony SHALL enforce permission preflight before Codex starts

`artemis-symphony` SHALL validate the effective Codex permission boundary for each worker attempt after the issue workspace is known and before launching Codex app-server. The preflight SHALL reject attempts that expand writable roots, network access, or danger-full-access sandbox behavior without explicit workflow acknowledgement.

Permission preflight failures SHALL be handled as normal attempt failures: Symphony SHALL NOT launch Codex app-server, SHALL run the normal attempt cleanup hook when applicable, and SHALL route the failure through run history, low-sensitivity summary output, and retry scheduling.

#### Scenario: Worker attempt uses default workspace sandbox

- **WHEN** workflow does not configure `codex.turn_sandbox_policy`
- **THEN** permission preflight SHALL accept the generated workspace-scoped `workspaceWrite` policy for the current issue workspace

#### Scenario: Worker attempt configures extra writable roots

- **WHEN** workflow configures writable roots outside the current issue workspace
- **THEN** permission preflight SHALL fail unless each extra root is covered by `permissions.allowed_writable_roots`
- **AND** the failed run summary SHALL expose the configured writable roots for low-sensitivity audit

#### Scenario: Worker attempt enables network access

- **WHEN** workflow configures `codex.turn_sandbox_policy.networkAccess` as enabled
- **THEN** permission preflight SHALL fail unless `permissions.network_access_reason` is configured

#### Scenario: Worker attempt requests danger full access

- **WHEN** workflow maps to `danger-full-access` thread sandbox or `dangerFullAccess` turn sandbox
- **THEN** permission preflight SHALL fail unless `permissions.allow_danger_full_access` is `true`
