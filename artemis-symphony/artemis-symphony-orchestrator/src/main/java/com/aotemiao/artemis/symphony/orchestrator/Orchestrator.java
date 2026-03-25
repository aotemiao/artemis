package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.config.DispatchPreflight;
import com.aotemiao.artemis.symphony.config.LinearCommentRenderer;
import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.WorkspaceKeys;
import com.aotemiao.artemis.symphony.core.model.CodexTotals;
import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.core.validation.DispatchValidation;
import com.aotemiao.artemis.symphony.tracker.TrackerClient;
import com.aotemiao.artemis.symphony.tracker.TrackerResult;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * 单一权威的编排器：轮询 tick、协调（reconcile）、下发任务、重试。见 SPEC 第 7、8、16 节。
 */
public class Orchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Orchestrator.class);

    private final SymphonyRuntimeHolder runtimeHolder;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "WorkspaceManager is a shared orchestrator collaborator injected and not exposed.")
    private final WorkspaceManager workspaceManager;

    private final AgentRunner agentRunner;

    private final Map<String, RunningEntry> running = new ConcurrentHashMap<>();
    private final Set<String> claimed = ConcurrentHashMap.newKeySet();
    private final Map<String, RetryEntry> retryAttempts = new ConcurrentHashMap<>();
    private final Set<String> completed = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unused")
    private Object codexRateLimits = null;

    private final ExecutorService workerPool = Executors.newCachedThreadPool();
    private final ScheduledExecutorService retryScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "symphony-retry");
        t.setDaemon(true);
        return t;
    });

    private final Object tickMonitor = new Object();
    private volatile boolean wakeImmediately;
    private volatile boolean started;
    private Thread tickThread;

    public Orchestrator(
            SymphonyRuntimeHolder runtimeHolder, WorkspaceManager workspaceManager, AgentRunner agentRunner) {
        this.runtimeHolder = runtimeHolder;
        this.workspaceManager = workspaceManager;
        this.agentRunner = agentRunner;
    }

    private ServiceConfig config() {
        return runtimeHolder.get().config();
    }

    private TrackerClient tracker() {
        return runtimeHolder.get().trackerClient();
    }

    public void start() {
        if (started) return;
        started = true;
        DispatchValidation validation = DispatchPreflight.validate(config());
        if (validation.ok()) {
            startupTerminalWorkspaceCleanup();
        } else {
            LOGGER.warn("action=dispatch_preflight outcome=degraded_start errors={}", validation.errors());
        }
        tickThread = new Thread(this::runTickLoop, "symphony-orchestrator-tick");
        tickThread.setDaemon(true);
        tickThread.start();
    }

    public void stop() {
        started = false;
        synchronized (tickMonitor) {
            tickMonitor.notifyAll();
        }
        retryScheduler.shutdown();
        workerPool.shutdown();
    }

    /**
     * 请求立即执行一整轮 tick（协调 + 在验证通过时可能调度）。若已有挂起的立即唤醒，则将本次合并。
     *
     * @return 若本次调用与已挂起的立即 tick 合并则返回 true（即 coalesced）
     */
    public boolean requestImmediateTick() {
        synchronized (tickMonitor) {
            boolean coalesced = wakeImmediately;
            wakeImmediately = true;
            tickMonitor.notifyAll();
            return coalesced;
        }
    }

    private void runTickLoop() {
        while (started) {
            try {
                runOneTick();
            } catch (Exception e) {
                LOGGER.error("action=tick_failed outcome=failed reason={}", e.toString(), e);
            }
            long delayMs;
            try {
                delayMs = config().getPollIntervalMs();
            } catch (Exception e) {
                delayMs = 30_000;
            }
            synchronized (tickMonitor) {
                try {
                    long deadline = System.currentTimeMillis() + delayMs;
                    while (started && System.currentTimeMillis() < deadline && !wakeImmediately) {
                        long waitMs = Math.max(1L, deadline - System.currentTimeMillis());
                        tickMonitor.wait(waitMs);
                    }
                    wakeImmediately = false;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void runOneTick() {
        reconcileRunningIssues();
        DispatchValidation validation = DispatchPreflight.validate(config());
        if (!validation.ok()) {
            LOGGER.warn("action=dispatch_preflight outcome=skipped errors={}", validation.errors());
            return;
        }
        var candidateResult =
                tracker().fetchCandidateIssues(config().getTrackerProjectSlug(), config().getTrackerActiveStates());
        if (!candidateResult.isSuccess() || candidateResult.value() == null) {
            LOGGER.warn("action=fetch_candidates outcome=failed");
            return;
        }
        List<Issue> candidates = sortForDispatch(candidateResult.value());
        int slots = config().getMaxConcurrentAgents() - running.size();
        for (Issue issue : candidates) {
            if (slots <= 0) break;
            if (shouldDispatch(issue)) {
                dispatchIssue(issue, null);
                slots--;
            }
        }
    }

    private void startupTerminalWorkspaceCleanup() {
        var result =
                tracker().fetchIssuesByStates(config().getTrackerProjectSlug(), config().getTrackerTerminalStates());
        if (!result.isSuccess() || result.value() == null) return;
        for (Issue issue : result.value()) {
            if (issue.identifier() != null) {
                workspaceManager.removeIssueWorkspaces(issue.identifier());
            }
        }
    }

    private void reconcileRunningIssues() {
        ServiceConfig cfg = config();
        TrackerClient tr = tracker();
        long stallTimeoutMs = cfg.getStallTimeoutMs();
        if (stallTimeoutMs > 0) {
            Instant now = Instant.now();
            for (RunningEntry e : running.values()) {
                long elapsed = e.lastCodexTimestamp != null
                        ? java.time.Duration.between(e.lastCodexTimestamp, now).toMillis()
                        : java.time.Duration.between(e.startedAt, now).toMillis();
                if (elapsed > stallTimeoutMs) {
                    putIssueMdc(e.issueId, e.identifier, e.sessionId);
                    try {
                        LOGGER.warn(
                                "action=stall_detected outcome=terminating issue_id={} issue_identifier={} session_id={}",
                                e.issueId,
                                e.identifier,
                                e.sessionId != null ? e.sessionId : "");
                        terminateRunning(e.issueId, true);
                    } finally {
                        clearIssueMdc();
                    }
                }
            }
        }
        if (running.isEmpty()) return;
        var result = tr.fetchIssueStatesByIds(new ArrayList<>(running.keySet()));
        if (!result.isSuccess() || result.value() == null) return;
        Set<String> visibleIssueIds = ConcurrentHashMap.newKeySet();
        List<String> terminal =
                cfg.getTrackerTerminalStates().stream().map(String::toLowerCase).toList();
        List<String> active =
                cfg.getTrackerActiveStates().stream().map(String::toLowerCase).toList();
        for (Issue issue : result.value()) {
            visibleIssueIds.add(issue.id());
            RunningEntry entry = running.get(issue.id());
            if (entry == null) continue;
            String stateNorm = issue.state() != null ? issue.state().toLowerCase() : "";
            if (terminal.contains(stateNorm)) {
                putIssueMdc(issue.id(), issue.identifier(), entry.sessionId);
                try {
                    LOGGER.info(
                            "action=reconcile_terminal issue_id={} issue_identifier={} session_id={}",
                            issue.id(),
                            issue.identifier(),
                            entry.sessionId != null ? entry.sessionId : "");
                    terminateRunning(issue.id(), true);
                } finally {
                    clearIssueMdc();
                }
            } else if (!issue.assignedToWorker()) {
                putIssueMdc(issue.id(), issue.identifier(), entry.sessionId);
                try {
                    LOGGER.info(
                            "action=reconcile_unrouted issue_id={} issue_identifier={} session_id={} assignee_id={}",
                            issue.id(),
                            issue.identifier(),
                            entry.sessionId != null ? entry.sessionId : "",
                            issue.assigneeId() != null ? issue.assigneeId() : "");
                    terminateRunning(issue.id(), false);
                } finally {
                    clearIssueMdc();
                }
            } else if (active.contains(stateNorm)) {
                entry.issue = issue;
            } else {
                putIssueMdc(issue.id(), issue.identifier(), entry.sessionId);
                try {
                    LOGGER.info(
                            "action=reconcile_non_active issue_id={} issue_identifier={} session_id={}",
                            issue.id(),
                            issue.identifier(),
                            entry.sessionId != null ? entry.sessionId : "");
                    terminateRunning(issue.id(), false);
                } finally {
                    clearIssueMdc();
                }
            }
        }
        for (String runningIssueId : new ArrayList<>(running.keySet())) {
            if (!visibleIssueIds.contains(runningIssueId)) {
                terminateRunning(runningIssueId, false);
            }
        }
    }

    private void terminateRunning(String issueId, boolean cleanupWorkspace) {
        RunningEntry entry = running.remove(issueId);
        if (entry == null) return;
        claimed.remove(issueId);
        if (cleanupWorkspace) {
            workspaceManager.removeWorkspace(resolveWorkspacePath(entry), entry.workerHost);
        }
    }

    private List<Issue> sortForDispatch(List<Issue> issues) {
        ServiceConfig cfg = config();
        List<String> activeLower =
                cfg.getTrackerActiveStates().stream().map(String::toLowerCase).toList();
        List<String> terminalLower =
                cfg.getTrackerTerminalStates().stream().map(String::toLowerCase).toList();
        return issues.stream()
                .filter(i -> hasRequiredFields(i)
                        && i.assignedToWorker()
                        && activeLower.contains(i.stateNormalized())
                        && !terminalLower.contains(i.stateNormalized()))
                .filter(i -> !running.containsKey(i.id()) && !claimed.contains(i.id()))
                .sorted(Comparator.comparing(Issue::priority, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Issue::createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Issue::identifier, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private boolean hasRequiredFields(Issue i) {
        return i.id() != null
                && !i.id().isBlank()
                && i.identifier() != null
                && !i.identifier().isBlank()
                && i.title() != null
                && i.state() != null;
    }

    private boolean shouldDispatch(Issue issue) {
        ServiceConfig cfg = config();
        if (running.containsKey(issue.id()) || claimed.contains(issue.id())) return false;
        if (!issue.assignedToWorker()) return false;
        int globalSlots = cfg.getMaxConcurrentAgents() - running.size();
        if (globalSlots <= 0) return false;
        if (!selectWorkerHost(null).hasCapacity()) return false;
        Map<String, Integer> byState = cfg.getMaxConcurrentAgentsByState();
        if (!byState.isEmpty()) {
            String stateNorm = issue.stateNormalized();
            int cap = byState.getOrDefault(stateNorm, cfg.getMaxConcurrentAgents());
            long count = running.values().stream()
                    .filter(e -> e.issue != null && stateNorm.equals(e.issue.stateNormalized()))
                    .count();
            if (count >= cap) return false;
        }
        if ("todo".equals(issue.stateNormalized())
                && issue.blockedBy() != null
                && !issue.blockedBy().isEmpty()) {
            List<String> terminal = cfg.getTrackerTerminalStates().stream()
                    .map(String::toLowerCase)
                    .toList();
            boolean allTerminal = issue.blockedBy().stream()
                    .allMatch(b ->
                            b.state() != null && terminal.contains(b.state().toLowerCase()));
            if (!allTerminal) return false;
        }
        return true;
    }

    private void dispatchIssue(Issue issue, Integer attempt) {
        dispatchIssue(issue, attempt, null);
    }

    private void dispatchIssue(Issue issue, Integer attempt, String preferredWorkerHost) {
        if (running.containsKey(issue.id()) || claimed.contains(issue.id())) return;
        WorkerSelection workerSelection = selectWorkerHost(preferredWorkerHost);
        if (!workerSelection.hasCapacity()) return;
        claimed.add(issue.id());
        retryAttempts.remove(issue.id());
        int retryAttempt = attempt != null ? attempt : 0;
        RunningEntry entry = new RunningEntry(issue.id(), issue.identifier(), issue, retryAttempt, Instant.now());
        entry.workerHost = workerSelection.workerHost();
        running.put(issue.id(), entry);

        final Map<String, String> parentMdc = MDC.getCopyOfContextMap();
        workerPool.submit(() -> {
            if (parentMdc != null) {
                MDC.setContextMap(parentMdc);
            }
            putIssueMdc(issue.id(), issue.identifier(), null);
            try {
                Issue executionIssue = claimTodoIssueIfNeeded(issue);
                entry.issue = executionIssue;
                LOGGER.info(
                        "action=worker_start issue_id={} issue_identifier={} tracker_state={}",
                        executionIssue.id(),
                        executionIssue.identifier(),
                        executionIssue.state());
                agentRunner.runAttempt(
                        executionIssue,
                        attempt,
                        workerSelection.workerHost(),
                        evt -> onCodexUpdate(issue.id(), evt),
                        runtimeInfo -> {
                            entry.workerHost = runtimeInfo.workerHost();
                            entry.workspacePath = runtimeInfo.workspacePath();
                        },
                        () -> onWorkerExit(issue.id(), true, null),
                        reason -> onWorkerExit(issue.id(), false, reason));
            } catch (Exception e) {
                LOGGER.warn(
                        "action=worker_exception issue_id={} issue_identifier={} reason={}",
                        issue.id(),
                        issue.identifier(),
                        e.toString());
                onWorkerExit(issue.id(), false, e.toString());
            } finally {
                clearIssueMdc();
                if (parentMdc != null) {
                    MDC.clear();
                }
            }
        });
    }

    Issue claimTodoIssueIfNeeded(Issue issue) {
        if (issue == null || !"todo".equals(issue.stateNormalized())) {
            return issue;
        }
        TrackerResult<Void> update = tracker().updateIssueState(issue.id(), "In Progress");
        if (!update.isSuccess()) {
            LOGGER.warn(
                    "action=issue_claim outcome=failed issue_id={} issue_identifier={} reason={}",
                    issue.id(),
                    issue.identifier(),
                    update.errorCode() + ": " + update.errorMessage());
            return issue;
        }
        var refreshed = tracker().fetchIssueStatesByIds(List.of(issue.id()));
        if (refreshed.isSuccess() && refreshed.value() != null && !refreshed.value().isEmpty()) {
            Issue refreshedIssue = refreshed.value().get(0);
            LOGGER.info(
                    "action=issue_claim outcome=claimed issue_id={} issue_identifier={} tracker_state={}",
                    refreshedIssue.id(),
                    refreshedIssue.identifier(),
                    refreshedIssue.state());
            return refreshedIssue;
        }
        Issue claimedIssue = withState(issue, "In Progress");
        LOGGER.info(
                "action=issue_claim outcome=claimed issue_id={} issue_identifier={} tracker_state={}",
                claimedIssue.id(),
                claimedIssue.identifier(),
                claimedIssue.state());
        return claimedIssue;
    }

    private static void putIssueMdc(String issueId, String issueIdentifier, String sessionId) {
        if (issueId != null) MDC.put("issue_id", issueId);
        if (issueIdentifier != null) MDC.put("issue_identifier", issueIdentifier);
        if (sessionId != null && !sessionId.isBlank()) MDC.put("session_id", sessionId);
        else MDC.remove("session_id");
    }

    private static void clearIssueMdc() {
        MDC.remove("issue_id");
        MDC.remove("issue_identifier");
        MDC.remove("session_id");
    }

    private static Issue withState(Issue issue, String stateName) {
        return new Issue(
                issue.id(),
                issue.identifier(),
                issue.title(),
                issue.description(),
                issue.priority(),
                stateName,
                issue.branchName(),
                issue.url(),
                issue.assigneeId(),
                issue.labels(),
                issue.blockedBy(),
                issue.assignedToWorker(),
                issue.createdAt(),
                Instant.now());
    }

    private void onCodexUpdate(String issueId, CodexUpdateEvent evt) {
        RunningEntry e = running.get(issueId);
        if (e == null) return;
        e.lastCodexEvent = evt.event();
        e.lastCodexTimestamp = evt.timestamp();
        e.lastCodexMessage = evt.payload() != null ? evt.payload().toString() : null;
        if ("turn_completed".equals(evt.event())) {
            e.turnCount++;
        }
        if (evt.payload() != null && evt.payload().containsKey("session_id")) {
            Object sid = evt.payload().get("session_id");
            if (sid != null) {
                e.sessionId = sid.toString();
                MDC.put("session_id", e.sessionId);
            }
        }
        if (evt.payload() != null && evt.payload().containsKey("worker_host")) {
            Object workerHost = evt.payload().get("worker_host");
            if (workerHost != null) {
                e.workerHost = workerHost.toString();
            }
        }
        if (evt.payload() != null && evt.payload().containsKey("codex_app_server_pid")) {
            Object pid = evt.payload().get("codex_app_server_pid");
            if (pid != null) {
                e.codexAppServerPid = pid.toString();
            }
        }
        if (evt.usage() != null) {
            e.codexInputTokens = ((Number) evt.usage().getOrDefault("input_tokens", 0)).longValue();
            e.codexOutputTokens = ((Number) evt.usage().getOrDefault("output_tokens", 0)).longValue();
            e.codexTotalTokens = ((Number) evt.usage().getOrDefault("total_tokens", 0)).longValue();
        }
        if (evt.payload() != null && evt.payload().containsKey("rate_limits")) {
            codexRateLimits = evt.payload().get("rate_limits");
        }
    }

    private void onWorkerExit(String issueId, boolean normal, String failureReason) {
        RunningEntry entry = running.remove(issueId);
        if (entry == null) return;
        claimed.remove(issueId);
        completed.add(issueId);
        LOGGER.info(
                "action=worker_exit outcome={} issue_id={} issue_identifier={} session_id={}",
                normal ? "completed" : "failed",
                entry.issueId,
                entry.identifier,
                entry.sessionId != null ? entry.sessionId : "");
        RetryEntry nextRetry;
        if (normal) {
            nextRetry = scheduleRetry(issueId, entry.identifier, 1, null, entry.workerHost, entry.workspacePath);
        } else {
            int nextAttempt = entry.retryAttempt + 1;
            String error = failureReason != null && !failureReason.isBlank() ? failureReason : "worker exited";
            nextRetry = scheduleRetry(
                    issueId, entry.identifier, nextAttempt, error, entry.workerHost, entry.workspacePath);
        }
        reportAttemptOutcome(entry, normal, nextRetry, failureReason);
    }

    private RetryEntry scheduleRetry(
            String issueId, String identifier, int attempt, String error, String workerHost, String workspacePath) {
        ServiceConfig cfg = config();
        long delayMs = attempt == 1 && error == null
                ? 1000
                : Math.min(10_000 * (1L << (attempt - 1)), cfg.getMaxRetryBackoffMs());
        long dueAtMs = System.currentTimeMillis() + delayMs;
        RetryEntry re = new RetryEntry(issueId, identifier, attempt, dueAtMs, null, error, workerHost, workspacePath);
        retryAttempts.put(issueId, re);
        retryScheduler.schedule(() -> onRetryTimer(issueId), delayMs, TimeUnit.MILLISECONDS);
        return re;
    }

    private void onRetryTimer(String issueId) {
        RetryEntry re = retryAttempts.remove(issueId);
        if (re == null) return;
        ServiceConfig cfg = config();
        TrackerClient tr = tracker();
        var result = tr.fetchCandidateIssues(cfg.getTrackerProjectSlug(), cfg.getTrackerActiveStates());
        if (!result.isSuccess() || result.value() == null) {
            scheduleRetry(issueId, re.identifier(), re.attempt() + 1, "retry poll failed", re.workerHost(), re.workspacePath());
            return;
        }
        Issue issue = result.value().stream()
                .filter(i -> i.id().equals(issueId))
                .findFirst()
                .orElse(null);
        if (issue == null) {
            claimed.remove(issueId);
            return;
        }
        if (running.size() >= cfg.getMaxConcurrentAgents()) {
            scheduleRetry(
                    issueId,
                    issue.identifier(),
                    re.attempt() + 1,
                    "no available orchestrator slots",
                    re.workerHost(),
                    re.workspacePath());
            return;
        }
        if (!selectWorkerHost(re.workerHost()).hasCapacity()) {
            scheduleRetry(
                    issueId,
                    issue.identifier(),
                    re.attempt() + 1,
                    "no available worker slots",
                    re.workerHost(),
                    re.workspacePath());
            return;
        }
        dispatchIssue(issue, re.attempt(), re.workerHost());
    }

    /** 按人类可读议题编号（如 MT-649）查找运行中条目。 */
    public RunningEntry findRunningByIdentifier(String identifier) {
        if (identifier == null) return null;
        for (RunningEntry e : running.values()) {
            if (identifier.equals(e.identifier)) return e;
        }
        return null;
    }

    public RetryEntry findRetryByIdentifier(String identifier) {
        if (identifier == null) return null;
        for (RetryEntry re : retryAttempts.values()) {
            if (identifier.equals(re.identifier())) return re;
        }
        return null;
    }

    public SymphonyRuntimeHolder getRuntimeHolder() {
        return runtimeHolder;
    }

    public Map<String, RunningEntry> getRunning() {
        return Map.copyOf(running);
    }

    public Set<String> getClaimed() {
        return Set.copyOf(claimed);
    }

    public Map<String, RetryEntry> getRetryAttempts() {
        return Map.copyOf(retryAttempts);
    }

    public CodexTotals getCodexTotals() {
        Instant now = Instant.now();
        long inputTokens = 0L;
        long outputTokens = 0L;
        long totalTokens = 0L;
        double secondsRunning = 0.0;
        for (RunningEntry entry : running.values()) {
            inputTokens += entry.codexInputTokens;
            outputTokens += entry.codexOutputTokens;
            totalTokens += entry.codexTotalTokens;
            secondsRunning += Math.max(
                    0.0, java.time.Duration.between(entry.startedAt, now).toMillis() / 1000.0);
        }
        return new CodexTotals(inputTokens, outputTokens, totalTokens, secondsRunning);
    }

    public Object getCodexRateLimits() {
        return codexRateLimits;
    }

    void reportAttemptOutcome(RunningEntry entry, boolean normal, RetryEntry retryEntry, String failureReason) {
        if (entry == null || entry.issue == null) {
            return;
        }
        ServiceConfig cfg = config();
        if (!cfg.isLinearCommentReportingEnabled()) {
            return;
        }
        String titleRegex = cfg.getLinearCommentIssueTitleRegex();
        if (titleRegex != null && !titleRegex.isBlank()) {
            try {
                String issueTitle = entry.issue.title() != null ? entry.issue.title() : "";
                if (!Pattern.compile(titleRegex).matcher(issueTitle).find()) {
                    return;
                }
            } catch (PatternSyntaxException e) {
                LOGGER.warn(
                        "action=linear_comment_filter outcome=invalid_regex issue_id={} issue_identifier={} regex={} reason={}",
                        entry.issueId,
                        entry.identifier,
                        titleRegex,
                        e.getMessage());
                return;
            }
        }

        Instant finishedAt = Instant.now();
        long durationSeconds = Math.max(
                0L, java.time.Duration.between(entry.startedAt, finishedAt).getSeconds());
        int attemptNumber = entry.retryAttempt + 1;

        Map<String, Object> attempt = new LinkedHashMap<>();
        attempt.put("number", attemptNumber);
        attempt.put("outcome", normal ? "completed" : "failed");
        attempt.put("outcome_text", normal ? "已完成本轮处理" : "本轮处理失败");
        attempt.put("started_at", entry.startedAt.toString());
        attempt.put("finished_at", finishedAt.toString());
        attempt.put("duration_seconds", durationSeconds);
        attempt.put("turn_count", entry.turnCount);
        attempt.put("session_id", entry.sessionId != null ? entry.sessionId : "");
        attempt.put("last_codex_event", entry.lastCodexEvent != null ? entry.lastCodexEvent : "");
        attempt.put("last_codex_at", entry.lastCodexTimestamp != null ? entry.lastCodexTimestamp.toString() : "");

        Map<String, Object> workspace = Map.of(
                "path",
                entry.workspacePath != null ? entry.workspacePath : resolveWorkspacePath(entry).toString(),
                "key",
                WorkspaceKeys.sanitize(entry.identifier),
                "worker_host",
                entry.workerHost != null ? entry.workerHost : "");

        Map<String, Object> usage = Map.of(
                "input_tokens", entry.codexInputTokens,
                "output_tokens", entry.codexOutputTokens,
                "total_tokens", entry.codexTotalTokens);

        Map<String, Object> retry = new LinkedHashMap<>();
        boolean retryScheduled = retryEntry != null;
        retry.put("scheduled", retryScheduled);
        retry.put("next_attempt", retryScheduled ? retryEntry.attempt() + 1 : 0);
        retry.put(
                "due_at",
                retryScheduled ? Instant.ofEpochMilli(retryEntry.dueAtMs()).toString() : "");
        retry.put(
                "delay_seconds",
                retryScheduled ? Math.max(0L, (retryEntry.dueAtMs() - finishedAt.toEpochMilli()) / 1000L) : 0L);
        retry.put(
                "error",
                failureReason != null && !failureReason.isBlank()
                        ? failureReason
                        : retryScheduled && retryEntry.error() != null ? retryEntry.error() : "");

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("attempt", attempt);
        context.put("workspace", workspace);
        context.put("usage", usage);
        context.put("retry", retry);

        try {
            String body = normal
                    ? LinearCommentRenderer.renderSuccess(cfg.getLinearCommentSuccessTemplate(), entry.issue, context)
                    : LinearCommentRenderer.renderFailure(cfg.getLinearCommentFailureTemplate(), entry.issue, context);
            var result = tracker().createIssueComment(entry.issueId, body);
            if (!result.isSuccess()) {
                LOGGER.warn(
                        "action=linear_comment_writeback outcome=failed issue_id={} issue_identifier={} reason={}",
                        entry.issueId,
                        entry.identifier,
                        result.errorMessage());
                return;
            }
            LOGGER.info(
                    "action=linear_comment_writeback outcome=posted issue_id={} issue_identifier={} comment_id={}",
                    entry.issueId,
                    entry.identifier,
                    result.value() != null ? result.value() : "");
        } catch (LinearCommentRenderer.CommentRenderException e) {
            LOGGER.warn(
                    "action=linear_comment_render outcome=failed code={} issue_id={} issue_identifier={} reason={}",
                    e.getCode(),
                    entry.issueId,
                    entry.identifier,
                    e.getMessage());
        } catch (Exception e) {
            LOGGER.warn(
                    "action=linear_comment_writeback outcome=failed issue_id={} issue_identifier={} reason={}",
                    entry.issueId,
                    entry.identifier,
                    e.toString());
        }
    }

    private WorkerSelection selectWorkerHost(String preferredWorkerHost) {
        List<String> hosts = config().getWorkerSshHosts();
        if (hosts.isEmpty()) {
            return new WorkerSelection(true, null);
        }
        List<String> availableHosts = hosts.stream().filter(this::workerHostSlotsAvailable).toList();
        if (availableHosts.isEmpty()) {
            return new WorkerSelection(false, null);
        }
        if (preferredWorkerHost != null && !preferredWorkerHost.isBlank() && availableHosts.contains(preferredWorkerHost)) {
            return new WorkerSelection(true, preferredWorkerHost);
        }
        String leastLoaded = availableHosts.stream()
                .min(Comparator.comparingLong(this::runningWorkerHostCount))
                .orElse(null);
        return new WorkerSelection(true, leastLoaded);
    }

    private boolean workerHostSlotsAvailable(String workerHost) {
        Integer limit = config().getWorkerMaxConcurrentAgentsPerHost();
        if (workerHost == null || limit == null || limit <= 0) {
            return true;
        }
        return runningWorkerHostCount(workerHost) < limit;
    }

    private long runningWorkerHostCount(String workerHost) {
        if (workerHost == null) {
            return 0L;
        }
        return running.values().stream().filter(entry -> workerHost.equals(entry.workerHost)).count();
    }

    private Path resolveWorkspacePath(RunningEntry entry) {
        if (entry.workspacePath != null && !entry.workspacePath.isBlank()) {
            return Path.of(entry.workspacePath);
        }
        return workspaceManager.getWorkspaceRoot().resolve(WorkspaceKeys.sanitize(entry.identifier)).normalize();
    }

    private record WorkerSelection(boolean hasCapacity, String workerHost) {}
}
