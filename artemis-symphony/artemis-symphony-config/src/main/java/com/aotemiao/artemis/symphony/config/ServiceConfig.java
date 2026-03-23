package com.aotemiao.artemis.symphony.config;

import static com.aotemiao.artemis.symphony.config.ConfigResolver.expandHome;
import static com.aotemiao.artemis.symphony.config.ConfigResolver.getNested;
import static com.aotemiao.artemis.symphony.config.ConfigResolver.resolveEnv;

import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 工作流配置的强类型访问器（默认值与环境变量解析）。见 SPEC 第 5.3、6.4 节。 */
public final class ServiceConfig {

    private static final String LINEAR_DEFAULT_ENDPOINT = "https://api.linear.app/graphql";
    private static final List<String> DEFAULT_ACTIVE_STATES = List.of("Todo", "In Progress");
    private static final List<String> DEFAULT_TERMINAL_STATES =
            List.of("Closed", "Cancelled", "Canceled", "Duplicate", "Done");
    private static final int DEFAULT_POLL_INTERVAL_MS = 30_000;
    private static final String DEFAULT_WORKSPACE_ROOT = "/symphony_workspaces";
    private static final int DEFAULT_HOOKS_TIMEOUT_MS = 60_000;
    private static final int DEFAULT_MAX_CONCURRENT_AGENTS = 10;
    private static final int DEFAULT_MAX_RETRY_BACKOFF_MS = 300_000;
    private static final int DEFAULT_MAX_TURNS = 20;
    private static final String DEFAULT_CODEX_COMMAND = "codex app-server";
    private static final int DEFAULT_TURN_TIMEOUT_MS = 3_600_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5_000;
    private static final int DEFAULT_STALL_TIMEOUT_MS = 300_000;

    private final WorkflowDefinition definition;

    public ServiceConfig(WorkflowDefinition definition) {
        this.definition = definition;
    }

    public Map<String, Object> getRawConfig() {
        return definition.config();
    }

    public String getPromptTemplate() {
        String t = definition.promptTemplate();
        return (t == null || t.isBlank()) ? "You are working on an issue from Linear." : t.trim();
    }

    // --- tracker ---
    public String getTrackerKind() {
        return stringOrNull(getNested(definition.config(), "tracker.kind"));
    }

    public String getTrackerEndpoint() {
        if (!"linear".equals(getTrackerKind())) {
            return stringOrNull(getNested(definition.config(), "tracker.endpoint"));
        }
        String e = stringOrNull(getNested(definition.config(), "tracker.endpoint"));
        return e != null && !e.isBlank() ? e : LINEAR_DEFAULT_ENDPOINT;
    }

    public String getTrackerApiKey() {
        String raw = stringOrNull(getNested(definition.config(), "tracker.api_key"));
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String resolved = resolveEnv(raw);
        return resolved != null && !resolved.isBlank() ? resolved : null;
    }

    public String getTrackerProjectSlug() {
        return stringOrNull(getNested(definition.config(), "tracker.project_slug"));
    }

    @SuppressWarnings("unchecked")
    public List<String> getTrackerActiveStates() {
        Object o = getNested(definition.config(), "tracker.active_states");
        if (o instanceof List<?> list) {
            return list.stream()
                    .map(x -> x == null ? "" : x.toString().trim())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(DEFAULT_ACTIVE_STATES);
    }

    @SuppressWarnings("unchecked")
    public List<String> getTrackerTerminalStates() {
        Object o = getNested(definition.config(), "tracker.terminal_states");
        if (o instanceof List<?> list) {
            return list.stream()
                    .map(x -> x == null ? "" : x.toString().trim())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(DEFAULT_TERMINAL_STATES);
    }

    // --- polling ---
    public int getPollIntervalMs() {
        return intFromConfig("polling.interval_ms", DEFAULT_POLL_INTERVAL_MS);
    }

    // --- workspace ---
    public Path getWorkspaceRoot() {
        Object o = getNested(definition.config(), "workspace.root");
        String s = o != null ? resolveEnv(o.toString().trim()) : null;
        if (s == null || s.isBlank()) {
            s = DEFAULT_WORKSPACE_ROOT;
        }
        s = expandHome(s);
        return Path.of(s);
    }

    // --- hooks ---
    public String getHookAfterCreate() {
        return multilineString(getNested(definition.config(), "hooks.after_create"));
    }

    public String getHookBeforeRun() {
        return multilineString(getNested(definition.config(), "hooks.before_run"));
    }

    public String getHookAfterRun() {
        return multilineString(getNested(definition.config(), "hooks.after_run"));
    }

    public String getHookBeforeRemove() {
        return multilineString(getNested(definition.config(), "hooks.before_remove"));
    }

    public int getHooksTimeoutMs() {
        int v = intFromConfig("hooks.timeout_ms", DEFAULT_HOOKS_TIMEOUT_MS);
        return v > 0 ? v : DEFAULT_HOOKS_TIMEOUT_MS;
    }

    // --- agent ---
    public int getMaxConcurrentAgents() {
        return intFromConfig("agent.max_concurrent_agents", DEFAULT_MAX_CONCURRENT_AGENTS);
    }

    public int getMaxTurns() {
        return intFromConfig("agent.max_turns", DEFAULT_MAX_TURNS);
    }

    public int getMaxRetryBackoffMs() {
        return intFromConfig("agent.max_retry_backoff_ms", DEFAULT_MAX_RETRY_BACKOFF_MS);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> getMaxConcurrentAgentsByState() {
        Object o = getNested(definition.config(), "agent.max_concurrent_agents_by_state");
        if (o instanceof Map<?, ?> map) {
            Map<String, Integer> out = new java.util.HashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                String state = e.getKey() == null ? "" : e.getKey().toString().toLowerCase();
                if (state.isEmpty()) continue;
                int val = toPositiveInt(e.getValue());
                if (val > 0) out.put(state, val);
            }
            return Map.copyOf(out);
        }
        return Map.of();
    }

    // --- codex ---
    public String getCodexCommand() {
        String c = stringOrNull(getNested(definition.config(), "codex.command"));
        return (c != null && !c.isBlank()) ? c.trim() : DEFAULT_CODEX_COMMAND;
    }

    public int getTurnTimeoutMs() {
        return intFromConfig("codex.turn_timeout_ms", DEFAULT_TURN_TIMEOUT_MS);
    }

    public int getReadTimeoutMs() {
        return intFromConfig("codex.read_timeout_ms", DEFAULT_READ_TIMEOUT_MS);
    }

    public int getStallTimeoutMs() {
        return intFromConfig("codex.stall_timeout_ms", DEFAULT_STALL_TIMEOUT_MS);
    }

    public String getCodexApprovalPolicy() {
        return stringOrNull(getNested(definition.config(), "codex.approval_policy"));
    }

    public String getCodexThreadSandbox() {
        return stringOrNull(getNested(definition.config(), "codex.thread_sandbox"));
    }

    public Object getCodexTurnSandboxPolicy() {
        return getNested(definition.config(), "codex.turn_sandbox_policy");
    }

    // --- server (extension) ---
    public Integer getServerPort() {
        Object o = getNested(definition.config(), "server.port");
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // --- helpers ---
    private static String stringOrNull(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static String multilineString(Object o) {
        if (o == null) return null;
        String s = o.toString();
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private int intFromConfig(String key, int defaultValue) {
        Object o = getNested(definition.config(), key);
        if (o == null) return defaultValue;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int toPositiveInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) {
            int v = n.intValue();
            return v > 0 ? v : 0;
        }
        try {
            int v = Integer.parseInt(o.toString().trim());
            return v > 0 ? v : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
