package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.config.DispatchPreflight;
import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.CodexTotals;
import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.core.validation.DispatchValidation;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Single-authority orchestrator: poll tick, reconcile, dispatch, retry. SPEC Section 7, 8, 16.
 */
public class Orchestrator {

    private final ServiceConfig config;
    private final LinearTrackerClient tracker;
    private final WorkspaceManager workspaceManager;
    private final AgentRunner agentRunner;

    private final Map<String, RunningEntry> running = new ConcurrentHashMap<>();
    private final Set<String> claimed = ConcurrentHashMap.newKeySet();
    private final Map<String, RetryEntry> retryAttempts = new ConcurrentHashMap<>();
    private final Set<String> completed = ConcurrentHashMap.newKeySet();
    private CodexTotals codexTotals = CodexTotals.zero();
    private Object codexRateLimits = null;

    private final ExecutorService workerPool = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean started;

    public Orchestrator(ServiceConfig config, LinearTrackerClient tracker, WorkspaceManager workspaceManager, AgentRunner agentRunner) {
        this.config = config;
        this.tracker = tracker;
        this.workspaceManager = workspaceManager;
        this.agentRunner = agentRunner;
    }

    public void start() {
        if (started) return;
        started = true;
        DispatchValidation validation = DispatchPreflight.validate(config);
        if (!validation.ok()) {
            throw new IllegalStateException("Dispatch validation failed: " + validation.errors());
        }
        startupTerminalWorkspaceCleanup();
        scheduler.schedule(this::onTick, 0, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        started = false;
        scheduler.shutdown();
        workerPool.shutdown();
        for (RunningEntry e : running.values()) {
            // could cancel worker if we stored Future
        }
    }

    private void startupTerminalWorkspaceCleanup() {
        var result = tracker.fetchIssuesByStates(config.getTrackerProjectSlug(), config.getTrackerTerminalStates());
        if (!result.isSuccess() || result.value() == null) return;
        for (Issue issue : result.value()) {
            if (issue.identifier() != null) {
                workspaceManager.removeWorkspace(workspaceManager.getWorkspaceRoot().resolve(com.aotemiao.artemis.symphony.core.WorkspaceKeys.sanitize(issue.identifier())));
            }
        }
    }

    private void onTick() {
        if (!started) return;
        reconcileRunningIssues();
        DispatchValidation validation = DispatchPreflight.validate(config);
        if (!validation.ok()) {
            scheduler.schedule(this::onTick, config.getPollIntervalMs(), TimeUnit.MILLISECONDS);
            return;
        }
        var candidateResult = tracker.fetchCandidateIssues(config.getTrackerProjectSlug(), config.getTrackerActiveStates());
        if (!candidateResult.isSuccess() || candidateResult.value() == null) {
            scheduler.schedule(this::onTick, config.getPollIntervalMs(), TimeUnit.MILLISECONDS);
            return;
        }
        List<Issue> candidates = sortForDispatch(candidateResult.value());
        int slots = config.getMaxConcurrentAgents() - running.size();
        for (Issue issue : candidates) {
            if (slots <= 0) break;
            if (shouldDispatch(issue)) {
                dispatchIssue(issue, null);
                slots--;
            }
        }
        scheduler.schedule(this::onTick, config.getPollIntervalMs(), TimeUnit.MILLISECONDS);
    }

    private void reconcileRunningIssues() {
        long stallTimeoutMs = config.getStallTimeoutMs();
        if (stallTimeoutMs > 0) {
            Instant now = Instant.now();
            for (RunningEntry e : running.values()) {
                long elapsed = e.lastCodexTimestamp != null
                        ? java.time.Duration.between(e.lastCodexTimestamp, now).toMillis()
                        : java.time.Duration.between(e.startedAt, now).toMillis();
                if (elapsed > stallTimeoutMs) {
                    terminateRunning(e.issueId, true);
                }
            }
        }
        if (running.isEmpty()) return;
        var result = tracker.fetchIssueStatesByIds(new ArrayList<>(running.keySet()));
        if (!result.isSuccess() || result.value() == null) return;
        List<String> terminal = config.getTrackerTerminalStates().stream().map(String::toLowerCase).toList();
        List<String> active = config.getTrackerActiveStates().stream().map(String::toLowerCase).toList();
        for (Issue issue : result.value()) {
            RunningEntry entry = running.get(issue.id());
            if (entry == null) continue;
            String stateNorm = issue.state() != null ? issue.state().toLowerCase() : "";
            if (terminal.contains(stateNorm)) {
                terminateRunning(issue.id(), true);
            } else if (active.contains(stateNorm)) {
                entry.issue = issue;
            } else {
                terminateRunning(issue.id(), false);
            }
        }
    }

    private void terminateRunning(String issueId, boolean cleanupWorkspace) {
        RunningEntry entry = running.remove(issueId);
        if (entry == null) return;
        claimed.remove(issueId);
        if (cleanupWorkspace) {
            workspaceManager.removeWorkspace(workspaceManager.getWorkspaceRoot().resolve(com.aotemiao.artemis.symphony.core.WorkspaceKeys.sanitize(entry.identifier)));
        }
    }

    private List<Issue> sortForDispatch(List<Issue> issues) {
        List<String> activeLower = config.getTrackerActiveStates().stream().map(String::toLowerCase).toList();
        List<String> terminalLower = config.getTrackerTerminalStates().stream().map(String::toLowerCase).toList();
        return issues.stream()
                .filter(i -> hasRequiredFields(i) && activeLower.contains(i.stateNormalized()) && !terminalLower.contains(i.stateNormalized()))
                .filter(i -> !running.containsKey(i.id()) && !claimed.contains(i.id()))
                .sorted(Comparator
                        .comparing(Issue::priority, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Issue::createdAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Issue::identifier, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private boolean hasRequiredFields(Issue i) {
        return i.id() != null && !i.id().isBlank()
                && i.identifier() != null && !i.identifier().isBlank()
                && i.title() != null && i.state() != null;
    }

    private boolean shouldDispatch(Issue issue) {
        if (running.containsKey(issue.id()) || claimed.contains(issue.id())) return false;
        int globalSlots = config.getMaxConcurrentAgents() - running.size();
        if (globalSlots <= 0) return false;
        Map<String, Integer> byState = config.getMaxConcurrentAgentsByState();
        if (!byState.isEmpty()) {
            String stateNorm = issue.stateNormalized();
            int cap = byState.getOrDefault(stateNorm, config.getMaxConcurrentAgents());
            long count = running.values().stream().filter(e -> e.issue != null && stateNorm.equals(e.issue.stateNormalized())).count();
            if (count >= cap) return false;
        }
        if ("todo".equals(issue.stateNormalized()) && issue.blockedBy() != null && !issue.blockedBy().isEmpty()) {
            List<String> terminal = config.getTrackerTerminalStates().stream().map(String::toLowerCase).toList();
            boolean allTerminal = issue.blockedBy().stream()
                    .allMatch(b -> b.state() != null && terminal.contains(b.state().toLowerCase()));
            if (!allTerminal) return false;
        }
        return true;
    }

    private void dispatchIssue(Issue issue, Integer attempt) {
        if (running.containsKey(issue.id()) || claimed.contains(issue.id())) return;
        claimed.add(issue.id());
        retryAttempts.remove(issue.id());
        int retryAttempt = attempt != null ? attempt : 0;
        RunningEntry entry = new RunningEntry(issue.id(), issue.identifier(), issue, retryAttempt, Instant.now());
        running.put(issue.id(), entry);

        workerPool.submit(() -> {
            try {
                agentRunner.runAttempt(
                        issue,
                        attempt,
                        evt -> onCodexUpdate(issue.id(), evt),
                        () -> onWorkerExit(issue.id(), true),
                        () -> onWorkerExit(issue.id(), false));
            } catch (Exception e) {
                onWorkerExit(issue.id(), false);
            }
        });
    }

    private void onCodexUpdate(String issueId, CodexUpdateEvent evt) {
        RunningEntry e = running.get(issueId);
        if (e == null) return;
        e.lastCodexEvent = evt.event();
        e.lastCodexTimestamp = evt.timestamp();
        e.lastCodexMessage = evt.payload() != null ? evt.payload().toString() : null;
        if (evt.usage() != null) {
            e.codexInputTokens = ((Number) evt.usage().getOrDefault("input_tokens", 0)).longValue();
            e.codexOutputTokens = ((Number) evt.usage().getOrDefault("output_tokens", 0)).longValue();
            e.codexTotalTokens = ((Number) evt.usage().getOrDefault("total_tokens", 0)).longValue();
        }
    }

    private void onWorkerExit(String issueId, boolean normal) {
        RunningEntry entry = running.remove(issueId);
        if (entry == null) return;
        claimed.remove(issueId);
        completed.add(issueId);
        if (normal) {
            scheduleRetry(issueId, entry.identifier, 1, null);
        } else {
            int nextAttempt = entry.retryAttempt + 1;
            scheduleRetry(issueId, entry.identifier, nextAttempt, "worker exited");
        }
    }

    private void scheduleRetry(String issueId, String identifier, int attempt, String error) {
        long delayMs = attempt == 1 && error == null ? 1000 : Math.min(10_000 * (1L << (attempt - 1)), config.getMaxRetryBackoffMs());
        long dueAtMs = System.currentTimeMillis() + delayMs;
        RetryEntry re = new RetryEntry(issueId, identifier, attempt, dueAtMs, null, error);
        retryAttempts.put(issueId, re);
        scheduler.schedule(() -> onRetryTimer(issueId), dueAtMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    private void onRetryTimer(String issueId) {
        RetryEntry re = retryAttempts.remove(issueId);
        if (re == null) return;
        var result = tracker.fetchCandidateIssues(config.getTrackerProjectSlug(), config.getTrackerActiveStates());
        if (!result.isSuccess() || result.value() == null) {
            scheduleRetry(issueId, re.identifier(), re.attempt() + 1, "retry poll failed");
            return;
        }
        Issue issue = result.value().stream().filter(i -> i.id().equals(issueId)).findFirst().orElse(null);
        if (issue == null) {
            claimed.remove(issueId);
            return;
        }
        if (running.size() >= config.getMaxConcurrentAgents()) {
            scheduleRetry(issueId, issue.identifier(), re.attempt() + 1, "no available orchestrator slots");
            return;
        }
        dispatchIssue(issue, re.attempt());
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
        return codexTotals;
    }
}
