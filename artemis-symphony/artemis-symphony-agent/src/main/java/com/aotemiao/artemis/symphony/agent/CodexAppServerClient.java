package com.aotemiao.artemis.symphony.agent;

import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Codex app-server 客户端：启动子进程，发送 initialize/thread/start/turn 等，按行读取 JSON 并产生事件。
 * 见 SPEC 第 10 节。
 */
public class CodexAppServerClient {

    private static final int MAX_LINE_BYTES = 10 * 1024 * 1024;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String command;
    private final Path workspacePath;
    private final int readTimeoutMs;
    private final int turnTimeoutMs;
    private final String approvalPolicy;
    private final String threadSandbox;
    private final Object turnSandboxPolicy;

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
        this.command = command;
        this.workspacePath = workspacePath;
        this.readTimeoutMs = readTimeoutMs;
        this.turnTimeoutMs = turnTimeoutMs;
        this.approvalPolicy = approvalPolicy != null ? approvalPolicy : "auto";
        this.threadSandbox = threadSandbox != null ? threadSandbox : "none";
        this.turnSandboxPolicy = turnSandboxPolicy;
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
            ProcessBuilder pb =
                    new ProcessBuilder()
                            .command("bash", "-lc", command)
                            .directory(workspacePath.toFile())
                            .redirectErrorStream(false);
            process = pb.start();
            running.set(true);

            stdin = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
            Thread stdoutReader = new Thread(() -> readStdout(process.getInputStream()));
            stdoutReader.setDaemon(true);
            stdoutReader.start();

            sendLine("{\"id\":1,\"method\":\"initialize\",\"params\":{\"clientInfo\":{\"name\":\"symphony\",\"version\":\"1.0\"},\"capabilities\":{}}}");
            JsonNode initResp = readResponse(1);
            if (initResp == null) {
                throw new CodexClientException("response_timeout", "initialize response timeout");
            }

            sendLine("{\"method\":\"initialized\",\"params\":{}}");

            String cwd = workspacePath.toAbsolutePath().toString().replace("\\", "/");
            ObjectNode threadParams = MAPPER.createObjectNode();
            threadParams.put("approvalPolicy", approvalPolicy);
            threadParams.put("sandbox", threadSandbox);
            threadParams.put("cwd", cwd);
            String threadReq = "{\"id\":2,\"method\":\"thread/start\",\"params\":" + threadParams.toString() + "}";
            sendLine(threadReq);
            JsonNode threadResp = readResponse(2);
            if (threadResp == null) {
                throw new CodexClientException("response_timeout", "thread/start response timeout");
            }
            JsonNode result = threadResp.path("result");
            String threadId = result.path("thread").path("id").asText(null);
            if (threadId == null) {
                throw new CodexClientException("response_error", "thread/start missing thread.id");
            }
            emit("session_started", Map.of("thread_id", threadId));
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
        String cwd = workspacePath.toAbsolutePath().toString().replace("\\", "/");
        ObjectNode inputItem = MAPPER.createObjectNode();
        inputItem.put("type", "text");
        inputItem.put("text", prompt);
        ObjectNode params = MAPPER.createObjectNode();
        params.put("threadId", threadId);
        params.set("input", MAPPER.createArrayNode().add(inputItem));
        params.put("cwd", cwd);
        params.put("title", title != null ? title : "Symphony");
        params.put("approvalPolicy", approvalPolicy);
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

    private final java.util.concurrent.BlockingQueue<JsonNode> messageQueue = new java.util.concurrent.LinkedBlockingQueue<>();

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
                            messageQueue.offer(node);
                        } catch (Exception e) {
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
            } catch (Exception ignored) {
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
        CodexUpdateEvent evt = new CodexUpdateEvent(method, Instant.now(), null, null, payload);
        for (CodexUpdateListener l : listeners) {
            try {
                l.onEvent(evt);
            } catch (Exception ignored) {
            }
        }
    }

    private void extractUsage(JsonNode msg, Map<String, Object> payload) {
        JsonNode params = msg.path("params");
        if (params.has("usage")) {
            payload.put("usage", MAPPER.convertValue(params.get("usage"), Map.class));
        }
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }

    public interface CodexUpdateListener {
        void onEvent(CodexUpdateEvent event);
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
