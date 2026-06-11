package com.aotemiao.artemis.symphony.api;

import com.aotemiao.artemis.symphony.persistence.RunHistoryEvent;
import com.aotemiao.artemis.symphony.persistence.RunHistoryMetrics;
import com.aotemiao.artemis.symphony.persistence.RunHistoryRecord;
import com.aotemiao.artemis.symphony.persistence.RunHistoryRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 运行历史只读接口与本地可视化页面。 */
@RestController
@RequestMapping
public class SymphonyHistoryController {

    private final RunHistoryRepository runHistoryRepository;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "RunHistoryRepository is a Spring-managed shared collaborator and is not exposed.")
    public SymphonyHistoryController(RunHistoryRepository runHistoryRepository) {
        this.runHistoryRepository = runHistoryRepository;
    }

    @GetMapping("/api/v1/history/runs")
    public ResponseEntity<Map<String, Object>> listRuns(@RequestParam(name = "limit", defaultValue = "50") int limit) {
        List<Map<String, Object>> runs = runHistoryRepository.listRecentRuns(limit).stream()
                .map(this::runBody)
                .toList();
        return ResponseEntity.ok(Map.of(
                "generated_at", Instant.now().toString(),
                "limit", boundedLimit(limit),
                "runs", runs));
    }

    @GetMapping("/api/v1/history/runs/{runId}/events")
    public ResponseEntity<Map<String, Object>> listEvents(
            @PathVariable("runId") String runId, @RequestParam(name = "limit", defaultValue = "200") int limit) {
        List<Map<String, Object>> events = runHistoryRepository.listRunEvents(runId, limit).stream()
                .map(this::eventBody)
                .toList();
        return ResponseEntity.ok(Map.of(
                "generated_at",
                Instant.now().toString(),
                "run_id",
                runId,
                "limit",
                boundedLimit(limit),
                "events",
                events));
    }

    @GetMapping("/api/v1/history/metrics")
    public ResponseEntity<Map<String, Object>> metrics(@RequestParam(name = "limit", defaultValue = "100") int limit) {
        RunHistoryMetrics metrics = runHistoryRepository.summarizeRecentRuns(limit);
        return ResponseEntity.ok(Map.of(
                "generated_at", Instant.now().toString(),
                "limit", metrics.limit(),
                "metrics", metricsBody(metrics)));
    }

    @GetMapping(value = "/runs", produces = MediaType.TEXT_HTML_VALUE)
    public String runsPage() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Symphony Runs</title>
                  <style>
                    :root {
                      color-scheme: light;
                      --bg: #f7f8fa;
                      --surface: #ffffff;
                      --line: #d7dce3;
                      --text: #1d2733;
                      --muted: #64748b;
                      --accent: #0f766e;
                      --failed: #b42318;
                      --ok: #166534;
                      --running: #1d4ed8;
                    }
                    * { box-sizing: border-box; }
                    body {
                      margin: 0;
                      background: var(--bg);
                      color: var(--text);
                      font: 14px/1.45 -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    }
                    header {
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      gap: 16px;
                      padding: 16px 24px;
                      border-bottom: 1px solid var(--line);
                      background: var(--surface);
                    }
                    h1 {
                      margin: 0;
                      font-size: 20px;
                      font-weight: 650;
                    }
                    nav {
                      display: flex;
                      align-items: center;
                      gap: 10px;
                    }
                    a, button {
                      color: var(--accent);
                      font: inherit;
                    }
                    button {
                      min-height: 34px;
                      padding: 0 12px;
                      border: 1px solid var(--accent);
                      border-radius: 6px;
                      background: var(--surface);
                      cursor: pointer;
                    }
                    main {
                      display: grid;
                      grid-template-columns: minmax(0, 1.15fr) minmax(360px, 0.85fr);
                      gap: 18px;
                      padding: 18px 24px 28px;
                    }
                    section {
                      min-width: 0;
                    }
                    .section-title {
                      display: flex;
                      justify-content: space-between;
                      align-items: baseline;
                      gap: 12px;
                      margin-bottom: 10px;
                    }
                    h2 {
                      margin: 0;
                      font-size: 16px;
                      font-weight: 650;
                    }
                    .muted { color: var(--muted); }
                    .table-wrap {
                      overflow: auto;
                      border: 1px solid var(--line);
                      background: var(--surface);
                    }
                    .metrics {
                      display: grid;
                      grid-template-columns: repeat(4, minmax(0, 1fr));
                      gap: 10px;
                      margin-bottom: 14px;
                    }
                    .metric {
                      min-width: 0;
                      padding: 10px 12px;
                      border: 1px solid var(--line);
                      background: var(--surface);
                    }
                    .metric-label {
                      color: var(--muted);
                      font-size: 12px;
                    }
                    .metric-value {
                      margin-top: 4px;
                      font-size: 20px;
                      font-weight: 650;
                    }
                    .status-bars {
                      display: flex;
                      flex-wrap: wrap;
                      gap: 8px;
                      margin: 0 0 14px;
                    }
                    .status-chip {
                      padding: 4px 8px;
                      border: 1px solid var(--line);
                      background: var(--surface);
                      color: var(--muted);
                    }
                    table {
                      width: 100%;
                      min-width: 900px;
                      border-collapse: collapse;
                    }
                    th, td {
                      padding: 9px 10px;
                      border-bottom: 1px solid var(--line);
                      text-align: left;
                      vertical-align: top;
                      white-space: nowrap;
                    }
                    th {
                      position: sticky;
                      top: 0;
                      background: #eef2f6;
                      color: #344256;
                      font-weight: 650;
                    }
                    tr {
                      cursor: pointer;
                    }
                    tr:hover, tr.selected {
                      background: #eef7f6;
                    }
                    .status {
                      display: inline-block;
                      min-width: 74px;
                      padding: 2px 8px;
                      border-radius: 999px;
                      text-align: center;
                      font-size: 12px;
                      border: 1px solid var(--line);
                    }
                    .status.completed { color: var(--ok); border-color: #86efac; background: #f0fdf4; }
                    .status.failed { color: var(--failed); border-color: #fecaca; background: #fff1f2; }
                    .status.running { color: var(--running); border-color: #bfdbfe; background: #eff6ff; }
                    .events {
                      min-height: 520px;
                      border: 1px solid var(--line);
                      background: var(--surface);
                      overflow: auto;
                    }
                    .event {
                      padding: 11px 12px;
                      border-bottom: 1px solid var(--line);
                    }
                    .event-title {
                      display: flex;
                      justify-content: space-between;
                      gap: 10px;
                      font-weight: 650;
                    }
                    pre {
                      max-height: 260px;
                      overflow: auto;
                      margin: 8px 0 0;
                      padding: 9px;
                      background: #f4f6f8;
                      border: 1px solid var(--line);
                      border-radius: 6px;
                      white-space: pre-wrap;
                      word-break: break-word;
                    }
                    .empty {
                      padding: 24px;
                      color: var(--muted);
                    }
                    @media (max-width: 920px) {
                      header { align-items: flex-start; flex-direction: column; }
                      main { grid-template-columns: 1fr; padding: 14px; }
                      table { min-width: 760px; }
                      .events { min-height: 320px; }
                      .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
                    }
                  </style>
                </head>
                <body>
                  <header>
                    <h1>Symphony 运行历史</h1>
                    <nav>
                      <a href="/api/v1/state">状态 JSON</a>
                      <a href="/api/v1/history/runs">历史 JSON</a>
                      <button type="button" id="refresh">刷新</button>
                    </nav>
                  </header>
                  <main>
                    <section>
                      <div class="section-title">
                        <h2>运行指标</h2>
                        <span id="metrics-window" class="muted">最近 100 条</span>
                      </div>
                      <div class="metrics" id="metrics">
                        <div class="metric"><div class="metric-label">总运行</div><div class="metric-value">-</div></div>
                        <div class="metric"><div class="metric-label">成功率</div><div class="metric-value">-</div></div>
                        <div class="metric"><div class="metric-label">平均耗时</div><div class="metric-value">-</div></div>
                        <div class="metric"><div class="metric-label">Token</div><div class="metric-value">-</div></div>
                      </div>
                      <div class="status-bars" id="status-bars"></div>
                      <div class="status-bars" id="failure-category-bars"></div>
                      <div class="section-title">
                        <h2>最近运行</h2>
                        <span id="summary" class="muted"></span>
                      </div>
                      <div class="table-wrap">
                        <table>
                          <thead>
                            <tr>
                              <th>状态</th>
                              <th>Issue</th>
                              <th>Attempt</th>
                              <th>Worker</th>
                              <th>Tokens</th>
                              <th>Started</th>
                              <th>Updated</th>
                            </tr>
                          </thead>
                          <tbody id="runs"></tbody>
                        </table>
                      </div>
                    </section>
                    <section>
                      <div class="section-title">
                        <h2>事件</h2>
                        <span id="selected" class="muted">选择一条运行记录</span>
                      </div>
                      <div id="events" class="events"><div class="empty">暂无事件</div></div>
                    </section>
                  </main>
                  <script>
                    const runsBody = document.getElementById('runs');
                    const eventsPanel = document.getElementById('events');
                    const summary = document.getElementById('summary');
                    const selected = document.getElementById('selected');
                    const metricsPanel = document.getElementById('metrics');
                    const metricsWindow = document.getElementById('metrics-window');
                    const statusBars = document.getElementById('status-bars');
                    const failureCategoryBars = document.getElementById('failure-category-bars');
                    let selectedRunId = '';

                    function text(value) {
                      return value === null || value === undefined || value === '' ? '-' : String(value);
                    }

                    function fmtTime(value) {
                      if (!value) return '-';
                      const date = new Date(value);
                      return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
                    }

                    function statusClass(status) {
                      const value = String(status || '').toLowerCase();
                      if (value.includes('fail')) return 'failed';
                      if (value.includes('complete') || value.includes('success')) return 'completed';
                      return 'running';
                    }

                    function pct(value) {
                      return `${Math.round(value * 10) / 10}%`;
                    }

                    function seconds(value) {
                      if (!value) return '-';
                      return `${Math.round(value * 10) / 10}s`;
                    }

                    function renderMetrics(data) {
                      const metrics = data.metrics || {};
                      const total = metrics.total_runs || 0;
                      const successRate = total === 0 ? 0 : (metrics.completed_runs || 0) * 100 / total;
                      metricsWindow.textContent = `最近 ${data.limit || 100} 条`;
                      const cells = [
                        ['总运行', total],
                        ['成功率', pct(successRate)],
                        ['平均耗时', seconds(metrics.average_duration_seconds)],
                        ['Token', metrics.tokens?.total_tokens || 0]
                      ];
                      metricsPanel.innerHTML = '';
                      for (const cell of cells) {
                        const item = document.createElement('div');
                        item.className = 'metric';
                        item.innerHTML = `<div class="metric-label">${cell[0]}</div><div class="metric-value">${cell[1]}</div>`;
                        metricsPanel.appendChild(item);
                      }
                      const statusCounts = metrics.status_counts || {};
                      statusBars.innerHTML = '';
                      for (const [status, count] of Object.entries(statusCounts)) {
                        const chip = document.createElement('span');
                        chip.className = 'status-chip';
                        chip.textContent = `${status}: ${count}`;
                        statusBars.appendChild(chip);
                      }
                      const failureCategoryCounts = metrics.failure_category_counts || {};
                      failureCategoryBars.innerHTML = '';
                      for (const [category, count] of Object.entries(failureCategoryCounts)) {
                        const chip = document.createElement('span');
                        chip.className = 'status-chip failed';
                        chip.textContent = `${category}: ${count}`;
                        failureCategoryBars.appendChild(chip);
                      }
                    }

                    function renderRuns(data) {
                      const runs = data.runs || [];
                      summary.textContent = `${runs.length} 条`;
                      runsBody.innerHTML = '';
                      if (runs.length === 0) {
                        runsBody.innerHTML = '<tr><td colspan="7" class="muted">暂无运行记录</td></tr>';
                        return;
                      }
                      for (const run of runs) {
                        const tr = document.createElement('tr');
                        tr.dataset.runId = run.run_id;
                        tr.innerHTML = `
                          <td><span class="status ${statusClass(run.status)}">${text(run.status)}</span></td>
                          <td>${text(run.issue_identifier)}<br><span class="muted">${text(run.issue_title)}</span></td>
                          <td>${Number(run.attempt || 0) + 1}</td>
                          <td>${text(run.worker_host)}<br><span class="muted">${text(run.workspace_path)}</span></td>
                          <td>${text(run.tokens?.total_tokens)}</td>
                          <td>${fmtTime(run.started_at)}</td>
                          <td>${fmtTime(run.updated_at)}</td>
                        `;
                        tr.addEventListener('click', () => selectRun(run, tr));
                        runsBody.appendChild(tr);
                      }
                    }

                    async function selectRun(run, row) {
                      selectedRunId = run.run_id;
                      for (const tr of runsBody.querySelectorAll('tr')) tr.classList.remove('selected');
                      row.classList.add('selected');
                      selected.textContent = `${text(run.issue_identifier)} / ${text(run.run_id)}`;
                      eventsPanel.innerHTML = '<div class="empty">加载中...</div>';
                      const response = await fetch(`/api/v1/history/runs/${encodeURIComponent(run.run_id)}/events?limit=200`);
                      renderEvents(await response.json());
                    }

                    function renderEvents(data) {
                      const events = data.events || [];
                      eventsPanel.innerHTML = '';
                      if (events.length === 0) {
                        eventsPanel.innerHTML = '<div class="empty">暂无事件</div>';
                        return;
                      }
                      for (const event of events) {
                        const item = document.createElement('div');
                        item.className = 'event';
                        item.innerHTML = `
                          <div class="event-title">
                            <span>${text(event.event_type)}</span>
                            <span class="muted">${fmtTime(event.event_time)}</span>
                          </div>
                          <div class="muted">session: ${text(event.session_id)}</div>
                          <pre>${text(event.payload)}</pre>
                        `;
                        eventsPanel.appendChild(item);
                      }
                    }

                    async function loadRuns() {
                      const [runsResponse, metricsResponse] = await Promise.all([
                        fetch('/api/v1/history/runs?limit=50'),
                        fetch('/api/v1/history/metrics?limit=100')
                      ]);
                      const data = await runsResponse.json();
                      renderRuns(data);
                      renderMetrics(await metricsResponse.json());
                      if (selectedRunId) {
                        const row = runsBody.querySelector(`tr[data-run-id="${CSS.escape(selectedRunId)}"]`);
                        if (row) row.click();
                      }
                    }

                    document.getElementById('refresh').addEventListener('click', loadRuns);
                    loadRuns().catch(error => {
                      runsBody.innerHTML = `<tr><td colspan="7" class="muted">${text(error)}</td></tr>`;
                    });
                  </script>
                </body>
                </html>
                """;
    }

    private Map<String, Object> runBody(RunHistoryRecord run) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("run_id", run.runId());
        body.put("issue_id", run.issueId());
        body.put("issue_identifier", run.issueIdentifier());
        body.put("issue_title", run.issueTitle());
        body.put("tracker_state", run.trackerState());
        body.put("status", run.status());
        body.put("attempt", run.attempt());
        body.put("worker_host", run.workerHost());
        body.put("workspace_path", run.workspacePath());
        body.put("thread_id", run.threadId());
        body.put("session_id", run.sessionId());
        body.put("codex_app_server_pid", run.codexAppServerPid());
        body.put(
                "tokens",
                Map.of(
                        "input_tokens", run.inputTokens(),
                        "output_tokens", run.outputTokens(),
                        "total_tokens", run.totalTokens()));
        body.put("failure_reason", run.failureReason());
        body.put("started_at", instantBody(run.startedAt()));
        body.put("updated_at", instantBody(run.updatedAt()));
        body.put("finished_at", instantBody(run.finishedAt()));
        return body;
    }

    private Map<String, Object> metricsBody(RunHistoryMetrics metrics) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("total_runs", metrics.totalRuns());
        body.put("completed_runs", metrics.completedRuns());
        body.put("failed_runs", metrics.failedRuns());
        body.put("terminated_runs", metrics.terminatedRuns());
        body.put("interrupted_runs", metrics.interruptedRuns());
        body.put("running_runs", metrics.runningRuns());
        body.put("retried_runs", metrics.retriedRuns());
        body.put(
                "tokens",
                Map.of(
                        "input_tokens", metrics.inputTokens(),
                        "output_tokens", metrics.outputTokens(),
                        "total_tokens", metrics.totalTokens()));
        body.put("average_duration_seconds", metrics.averageDurationSeconds());
        body.put("earliest_started_at", instantBody(metrics.earliestStartedAt()));
        body.put("latest_updated_at", instantBody(metrics.latestUpdatedAt()));
        body.put("status_counts", metrics.statusCounts());
        body.put("failure_category_counts", metrics.failureCategoryCounts());
        return body;
    }

    private Map<String, Object> eventBody(RunHistoryEvent event) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", event.id());
        body.put("run_id", event.runId());
        body.put("event_time", instantBody(event.eventTime()));
        body.put("event_type", event.eventType());
        body.put("session_id", event.sessionId());
        body.put("payload", event.payload());
        return body;
    }

    private static String instantBody(Instant instant) {
        return instant != null ? instant.toString() : "";
    }

    private static int boundedLimit(int limit) {
        if (limit <= 0) {
            return 50;
        }
        return Math.min(limit, 500);
    }
}
