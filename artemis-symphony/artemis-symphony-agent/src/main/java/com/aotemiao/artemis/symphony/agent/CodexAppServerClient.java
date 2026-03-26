package com.aotemiao.artemis.symphony.agent;

import com.aotemiao.artemis.symphony.core.SshClient;
import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Codex app-server 客户端：启动子进程，发送 initialize/thread/start/turn 等，按行读取 JSON 并产生事件。
 * 见 SPEC 第 10 节。
 */
public class CodexAppServerClient {

    private static final int MAX_LINE_BYTES = 10 * 1024 * 1024;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DEFAULT_APPROVAL_POLICY = "never";
    private static final String DEFAULT_THREAD_SANDBOX = "workspace-write";
    private static final String LEGACY_NONE_THREAD_SANDBOX = "danger-full-access";
    private static final String NON_INTERACTIVE_TOOL_INPUT_ANSWER =
            "This is a non-interactive session. Operator input is unavailable.";
    private static final System.Logger LOGGER = System.getLogger(CodexAppServerClient.class.getName());

    private final String command;
    private final Path workspacePath;
    private final int readTimeoutMs;
    private final int turnTimeoutMs;
    private final Object approvalPolicy;
    private final boolean autoApproveRequests;
    private final String threadSandbox;
    private final Object turnSandboxPolicy;
    private final DynamicToolExecutor dynamicToolExecutor;
    private final String workerHost;

    private Process process;
    private Writer stdin;
    private final CopyOnWriteArrayList<CodexUpdateListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CodexAppServerClient(
            String command,
            Path workspacePath,
            int readTimeoutMs,
            int turnTimeoutMs,
            String approvalPolicy,
            String threadSandbox,
            Object turnSandboxPolicy) {
        this(
                command,
                workspacePath,
                readTimeoutMs,
                turnTimeoutMs,
                approvalPolicy,
                threadSandbox,
                turnSandboxPolicy,
                null,
                null);
    }

    public CodexAppServerClient(
            String command,
            Path workspacePath,
            int readTimeoutMs,
            int turnTimeoutMs,
            String approvalPolicy,
            String threadSandbox,
            Object turnSandboxPolicy,
            DynamicToolExecutor dynamicToolExecutor) {
        this(
                command,
                workspacePath,
                readTimeoutMs,
                turnTimeoutMs,
                (Object) approvalPolicy,
                threadSandbox,
                turnSandboxPolicy,
                dynamicToolExecutor,
                null);
    }

    public CodexAppServerClient(
            String command,
            Path workspacePath,
            int readTimeoutMs,
            int turnTimeoutMs,
            Object approvalPolicy,
            String threadSandbox,
            Object turnSandboxPolicy,
            DynamicToolExecutor dynamicToolExecutor,
            String workerHost) {
        this.command = command;
        this.workspacePath = workspacePath;
        this.readTimeoutMs = readTimeoutMs;
        this.turnTimeoutMs = turnTimeoutMs;
        this.approvalPolicy = normalizeApprovalPolicy(approvalPolicy);
        this.autoApproveRequests = isNeverApprovalPolicy(this.approvalPolicy);
        this.threadSandbox = normalizeThreadSandbox(threadSandbox);
        this.turnSandboxPolicy = turnSandboxPolicy;
        this.dynamicToolExecutor = dynamicToolExecutor;
        this.workerHost = workerHost != null && !workerHost.isBlank() ? workerHost : null;
    }

    public void addListener(CodexUpdateListener listener) {
        if (listener != null) listeners.add(listener);
    }

    /**
     * Start the app-server process and perform handshake (initialize, thread/start). Returns
     * session id (thread_id - turn_id) or throws on failure.
     */
    public String startSession() throws CodexClientException {
        try {
            process = startProcess();
            running.set(true);

            stdin = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
            Thread stdoutReader = new Thread(() -> readStdout(process.getInputStream()));
            stdoutReader.setDaemon(true);
            stdoutReader.start();

            sendLine(
                    "{\"id\":1,\"method\":\"initialize\",\"params\":{\"clientInfo\":{\"name\":\"symphony\",\"version\":\"1.0\"},\"capabilities\":{\"experimentalApi\":true}}}");
            JsonNode initResp = readResponse(1);
            if (initResp == null) {
                throw new CodexClientException("response_timeout", "initialize response timeout");
            }
            throwIfErrorResponse(initResp, "initialize");

            sendLine("{\"method\":\"initialized\",\"params\":{}}");

            String cwd = workspaceCwd();
            ObjectNode threadParams = MAPPER.createObjectNode();
            threadParams.set("approvalPolicy", MAPPER.valueToTree(approvalPolicy));
            threadParams.put("sandbox", threadSandbox);
            threadParams.put("cwd", cwd);
            List<Map<String, Object>> dynamicTools = toolSpecifications();
            if (!dynamicTools.isEmpty()) {
                threadParams.set("dynamicTools", MAPPER.valueToTree(dynamicTools));
            }
            String threadReq = "{\"id\":2,\"method\":\"thread/start\",\"params\":" + threadParams.toString() + "}";
            sendLine(threadReq);
            JsonNode threadResp = readResponse(2);
            if (threadResp == null) {
                throw new CodexClientException("response_timeout", "thread/start response timeout");
            }
            throwIfErrorResponse(threadResp, "thread/start");
            JsonNode result = threadResp.path("result");
            String threadId = result.path("thread").path("id").asText(null);
            if (threadId == null) {
                throw new CodexClientException("response_error", "thread/start missing thread.id");
            }
            java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
            payload.put("thread_id", threadId);
            payload.put("codex_app_server_pid", String.valueOf(process.pid()));
            if (workerHost != null) {
                payload.put("worker_host", workerHost);
            }
            emit("session_started", Map.copyOf(payload));
            return threadId;
        } catch (CodexClientException e) {
            emit("startup_failed", Map.of("error", e.getMessage()));
            throw e;
        } catch (Exception e) {
            emit("startup_failed", Map.of("error", e.getMessage()));
            throw new CodexClientException("port_exit", e.getMessage(), e);
        }
    }

    /**
     * Start a turn with the given prompt. Blocks until turn completes or times out. Returns true on
     * success, false on failure/cancel/timeout.
     */
    public boolean runTurn(String threadId, String prompt, String title) throws CodexClientException {
        String cwd = workspaceCwd();
        ObjectNode inputItem = MAPPER.createObjectNode();
        inputItem.put("type", "text");
        inputItem.put("text", prompt);
        ObjectNode params = MAPPER.createObjectNode();
        params.put("threadId", threadId);
        params.set("input", MAPPER.createArrayNode().add(inputItem));
        params.put("cwd", cwd);
        params.put("title", title != null ? title : "Symphony");
        params.set("approvalPolicy", MAPPER.valueToTree(approvalPolicy));
        if (turnSandboxPolicy != null) {
            params.set("sandboxPolicy", MAPPER.valueToTree(turnSandboxPolicy));
        }
        try {
            sendLine("{\"id\":3,\"method\":\"turn/start\",\"params\":" + params.toString() + "}");
        } catch (Exception e) {
            throw new CodexClientException("response_error", e.getMessage(), e);
        }
        JsonNode turnResp = readResponse(3);
        if (turnResp == null) {
            emit("turn_ended_with_error", Map.of("reason", "response_timeout"));
            return false;
        }
        throwIfErrorResponse(turnResp, "turn/start");
        JsonNode result = turnResp.path("result");
        String turnId = result.path("turn").path("id").asText(null);
        String sessionId = threadId + " - " + (turnId != null ? turnId : "");

        long deadline = System.currentTimeMillis() + turnTimeoutMs;
        while (System.currentTimeMillis() < deadline && running.get()) {
            JsonNode msg = readMessage();
            if (msg == null) {
                continue;
            }
            String method = msg.path("method").asText(null);
            if (method != null) {
                switch (method) {
                    case "turn/completed" -> {
                        emit("turn_completed", Map.of());
                        return true;
                    }
                    case "turn/failed" -> {
                        emit("turn_failed", Map.of());
                        return false;
                    }
                    case "turn/cancelled" -> {
                        emit("turn_cancelled", Map.of());
                        return false;
                    }
                    case "item/tool/call" -> {
                        if (!handleDynamicToolCall(msg)) {
                            emit("turn_ended_with_error", Map.of("reason", "dynamic_tool_failed"));
                            return false;
                        }
                    }
                    case "item/commandExecution/requestApproval", "item/fileChange/requestApproval" -> {
                        if (!handleApprovalRequest(msg, "acceptForSession")) {
                            emit("turn_ended_with_error", Map.of("reason", "approval_required"));
                            return false;
                        }
                    }
                    case "execCommandApproval", "applyPatchApproval" -> {
                        if (!handleApprovalRequest(msg, "approved_for_session")) {
                            emit("turn_ended_with_error", Map.of("reason", "approval_required"));
                            return false;
                        }
                    }
                    case "item/tool/requestUserInput" -> {
                        if (!handleToolRequestUserInput(msg)) {
                            emit("turn_ended_with_error", Map.of("reason", "turn_input_required"));
                            return false;
                        }
                    }
                    default -> emitFromMessage(msg, sessionId);
                }
            }
        }
        emit("turn_ended_with_error", Map.of("reason", "turn_timeout"));
        return false;
    }

    public void stopSession() {
        running.set(false);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private void sendLine(String line) throws Exception {
        stdin.write(line + "\n");
        stdin.flush();
    }

    private void sendJson(Object payload) throws Exception {
        sendLine(MAPPER.writeValueAsString(payload));
    }

    private JsonNode readResponse(int expectedId) {
        long deadline = System.currentTimeMillis() + readTimeoutMs;
        while (System.currentTimeMillis() < deadline && running.get()) {
            JsonNode msg = readMessage();
            if (msg == null) continue;
            if (msg.has("id") && !msg.get("id").isNull() && msg.get("id").asInt(-1) == expectedId) {
                return msg;
            }
            emitFromMessage(msg, null);
        }
        return null;
    }

    private final java.util.concurrent.BlockingQueue<JsonNode> messageQueue =
            new java.util.concurrent.LinkedBlockingQueue<>();

    private JsonNode readMessage() {
        try {
            return messageQueue.poll(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void readStdout(java.io.InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder line = new StringBuilder();
            int c;
            while (running.get() && (c = reader.read()) != -1) {
                if (c == '\n') {
                    String s = line.toString().trim();
                    line.setLength(0);
                    if (s.length() > MAX_LINE_BYTES) continue;
                    if (!s.isEmpty()) {
                        try {
                            JsonNode node = MAPPER.readTree(s);
                            if (!messageQueue.offer(node)) {
                                emit("malformed", Map.of("error", "message_queue_full"));
                            }
                        } catch (IOException e) {
                            emit("malformed", Map.of("line", truncate(s, 200)));
                        }
                    }
                } else {
                    if (line.length() < MAX_LINE_BYTES) line.append((char) c);
                }
            }
        } catch (Exception e) {
            if (running.get()) {
                emit("malformed", Map.of("error", e.getMessage()));
            }
        }
    }

    private void emit(String event, Map<String, Object> payload) {
        CodexUpdateEvent evt = new CodexUpdateEvent(event, Instant.now(), null, null, payload);
        for (CodexUpdateListener l : listeners) {
            try {
                l.onEvent(evt);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "忽略事件监听器异常 event=" + event, e);
            }
        }
    }

    private void emitFromMessage(JsonNode msg, String sessionId) {
        String method = msg.path("method").asText("other_message");
        Map<String, Object> payload = new java.util.HashMap<>();
        if (msg.has("params")) {
            payload.put("params", MAPPER.convertValue(msg.get("params"), Map.class));
        }
        if (sessionId != null) payload.put("session_id", sessionId);
        extractUsage(msg, payload);
        extractRateLimits(msg, payload);
        CodexUpdateEvent evt = new CodexUpdateEvent(method, Instant.now(), null, null, payload);
        for (CodexUpdateListener l : listeners) {
            try {
                l.onEvent(evt);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "忽略消息监听器异常 method=" + method, e);
            }
        }
    }

    private boolean handleDynamicToolCall(JsonNode message) throws CodexClientException {
        JsonNode params = message.path("params");
        String toolName = firstNonBlank(
                params.path("name").asText(null), params.path("tool").asText(null));
        Object arguments = params.has("arguments") ? MAPPER.convertValue(params.get("arguments"), Object.class) : null;
        Map<String, Object> result = normalizeDynamicToolResult(
                dynamicToolExecutor != null
                        ? dynamicToolExecutor.execute(toolName, arguments)
                        : unsupportedToolResult(toolName));
        try {
            ObjectNode response = MAPPER.createObjectNode();
            if (message.has("id")) {
                response.set("id", message.get("id"));
            }
            response.set("result", MAPPER.valueToTree(result));
            sendJson(response);
            emit(
                    Boolean.TRUE.equals(result.get("success")) ? "tool_call_completed" : "tool_call_failed",
                    Map.of("tool", toolName != null ? toolName : "", "result", result));
            return true;
        } catch (Exception e) {
            throw new CodexClientException("dynamic_tool_failed", e.getMessage(), e);
        }
    }

    private boolean handleApprovalRequest(JsonNode message, String decision) throws CodexClientException {
        if (!autoApproveRequests) {
            emit("approval_required", Map.of("method", message.path("method").asText(""), "decision", decision));
            return false;
        }
        try {
            ObjectNode response = MAPPER.createObjectNode();
            if (message.has("id")) {
                response.set("id", message.get("id"));
            }
            response.set("result", MAPPER.valueToTree(Map.of("decision", decision)));
            sendJson(response);
            emit(
                    "approval_auto_approved",
                    Map.of("method", message.path("method").asText(""), "decision", decision));
            return true;
        } catch (Exception e) {
            throw new CodexClientException("approval_failed", e.getMessage(), e);
        }
    }

    private boolean handleToolRequestUserInput(JsonNode message) throws CodexClientException {
        JsonNode params = message.path("params");
        JsonNode questions = params.path("questions");
        if (!questions.isArray() || questions.isEmpty()) {
            emit("turn_input_required", Map.of("method", message.path("method").asText("")));
            return false;
        }
        ObjectNode answers = MAPPER.createObjectNode();
        for (JsonNode question : questions) {
            String questionId = question.path("id").asText(null);
            if (questionId == null || questionId.isBlank()) {
                emit(
                        "turn_input_required",
                        Map.of("method", message.path("method").asText("")));
                return false;
            }
            String selectedAnswer = selectToolInputAnswer(question);
            answers.set(questionId, MAPPER.valueToTree(Map.of("answers", List.of(selectedAnswer))));
        }
        try {
            ObjectNode response = MAPPER.createObjectNode();
            if (message.has("id")) {
                response.set("id", message.get("id"));
            }
            response.set("result", MAPPER.valueToTree(Map.of("answers", answers)));
            sendJson(response);
            emit(
                    "tool_input_auto_answered",
                    Map.of("method", message.path("method").asText("")));
            return true;
        } catch (Exception e) {
            throw new CodexClientException("tool_input_failed", e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> toolSpecifications() {
        if (dynamicToolExecutor == null) {
            return List.of();
        }
        List<Map<String, Object>> specifications = dynamicToolExecutor.toolSpecifications();
        return specifications != null ? List.copyOf(specifications) : List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeDynamicToolResult(Map<String, Object> result) {
        if (result == null) {
            return Map.of(
                    "success",
                    false,
                    "output",
                    "null",
                    "contentItems",
                    List.of(Map.of("type", "inputText", "text", "null")));
        }
        Object successValue = result.get("success");
        boolean success = successValue instanceof Boolean b && b;
        Object outputValue = result.get("output");
        String output = outputValue instanceof String s ? s : String.valueOf(result);
        Object contentItemsValue = result.get("contentItems");
        List<Map<String, Object>> contentItems;
        if (contentItemsValue instanceof List<?> list && list.stream().allMatch(Map.class::isInstance)) {
            contentItems = (List<Map<String, Object>>) contentItemsValue;
        } else {
            contentItems = List.of(Map.of("type", "inputText", "text", output));
        }
        return Map.of(
                "success", success,
                "output", output,
                "contentItems", contentItems);
    }

    private static Map<String, Object> unsupportedToolResult(String toolName) {
        String resolvedToolName = toolName != null ? toolName : "<unknown>";
        String output = """
                {
                  "error": {
                    "message": "Unsupported dynamic tool: %s."
                  }
                }
                """.formatted(resolvedToolName).trim();
        return Map.of(
                "success",
                false,
                "output",
                output,
                "contentItems",
                List.of(Map.of("type", "inputText", "text", output)));
    }

    private static String selectToolInputAnswer(JsonNode question) {
        JsonNode options = question.path("options");
        if (options.isArray()) {
            for (JsonNode option : options) {
                String label = option.path("label").asText(null);
                if ("Approve this Session".equals(label)) {
                    return label;
                }
            }
            JsonNode first = options.path(0);
            String firstLabel = first.path("label").asText(null);
            if (firstLabel != null && !firstLabel.isBlank()) {
                return firstLabel;
            }
        }
        return NON_INTERACTIVE_TOOL_INPUT_ANSWER;
    }

    private void extractUsage(JsonNode msg, Map<String, Object> payload) {
        JsonNode params = msg.path("params");
        if (params.has("usage")) {
            payload.put("usage", MAPPER.convertValue(params.get("usage"), Map.class));
            return;
        }
        JsonNode tokenUsage = params.path("tokenUsage");
        if (!tokenUsage.isMissingNode() && !tokenUsage.isNull()) {
            JsonNode totals = tokenUsage.path("total");
            if (totals.isMissingNode() || totals.isNull() || !totals.isObject()) {
                totals = tokenUsage.path("last");
            }
            if (totals.isObject()) {
                payload.put(
                        "usage",
                        Map.of(
                                "input_tokens", totals.path("inputTokens").asLong(0L),
                                "output_tokens", totals.path("outputTokens").asLong(0L),
                                "total_tokens", totals.path("totalTokens").asLong(0L)));
            }
        }
    }

    private void extractRateLimits(JsonNode msg, Map<String, Object> payload) {
        JsonNode rateLimits = msg.path("params").path("rateLimits");
        if (!rateLimits.isMissingNode() && !rateLimits.isNull() && rateLimits.isObject()) {
            payload.put("rate_limits", MAPPER.convertValue(rateLimits, Map.class));
        }
    }

    private static void throwIfErrorResponse(JsonNode response, String operation) throws CodexClientException {
        JsonNode error = response.path("error");
        if (error.isMissingNode() || error.isNull()) {
            return;
        }
        String message = error.path("message").asText(null);
        if (message == null || message.isBlank()) {
            message = operation + " returned error response";
        } else {
            message = operation + " failed: " + message;
        }
        throw new CodexClientException("response_error", message);
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private Process startProcess() throws Exception {
        if (workerHost == null) {
            return new ProcessBuilder()
                    .command("bash", "-lc", command)
                    .directory(workspacePath.toFile())
                    .redirectErrorStream(false)
                    .start();
        }
        return SshClient.processBuilder(workerHost, remoteLaunchCommand())
                .redirectErrorStream(false)
                .start();
    }

    private String workspaceCwd() {
        String cwd = workerHost == null
                ? workspacePath.toAbsolutePath().normalize().toString()
                : workspacePath.normalize().toString();
        return cwd.replace("\\", "/");
    }

    private String remoteLaunchCommand() {
        return """
                workspace=%s
                if [ "$workspace" = "~" ]; then
                  workspace="$HOME"
                elif [ "${workspace#\\~/}" != "$workspace" ]; then
                  workspace="$HOME/${workspace#\\~/}"
                fi
                cd "$workspace" && exec %s
                """.formatted(SshClient.shellEscape(workspacePath.toString()), command)
                .trim();
    }

    @SuppressWarnings("unchecked")
    private static Object normalizeApprovalPolicy(Object configuredValue) {
        if (configuredValue == null) {
            return DEFAULT_APPROVAL_POLICY;
        }
        if (configuredValue instanceof Map<?, ?> map) {
            return normalizeMap((Map<Object, Object>) map);
        }
        String value = configuredValue.toString().trim();
        if (value.isEmpty() || "auto".equalsIgnoreCase(value)) {
            return DEFAULT_APPROVAL_POLICY;
        }
        return value;
    }

    private static boolean isNeverApprovalPolicy(Object approvalPolicy) {
        return approvalPolicy instanceof String value && "never".equalsIgnoreCase(value);
    }

    private static String normalizeThreadSandbox(String configuredValue) {
        String value = configuredValue != null ? configuredValue.trim() : "";
        if (value.isEmpty()) {
            return DEFAULT_THREAD_SANDBOX;
        }
        if ("none".equalsIgnoreCase(value)) {
            return LEGACY_NONE_THREAD_SANDBOX;
        }
        return value;
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> normalizeMap(Map<Object, Object> raw) {
        java.util.Map<String, Object> normalized = new java.util.LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nestedMap) {
                normalized.put(key, normalizeMap((Map<Object, Object>) nestedMap));
            } else if (value instanceof List<?> nestedList) {
                normalized.put(key, normalizeList(nestedList));
            } else {
                normalized.put(key, value);
            }
        }
        return Map.copyOf(normalized);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> normalizeList(List<?> raw) {
        List<Object> normalized = new java.util.ArrayList<>(raw.size());
        for (Object value : raw) {
            if (value instanceof Map<?, ?> nestedMap) {
                normalized.add(normalizeMap((Map<Object, Object>) nestedMap));
            } else if (value instanceof List<?> nestedList) {
                normalized.add(normalizeList(nestedList));
            } else {
                normalized.add(value);
            }
        }
        return List.copyOf(normalized);
    }

    public interface CodexUpdateListener {
        void onEvent(CodexUpdateEvent event);
    }

    public interface DynamicToolExecutor {
        List<Map<String, Object>> toolSpecifications();

        Map<String, Object> execute(String toolName, Object arguments);
    }

    public static class CodexClientException extends Exception {
        private final String code;

        public CodexClientException(String code, String message) {
            super(message);
            this.code = code;
        }

        public CodexClientException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
