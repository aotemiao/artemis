package com.aotemiao.artemis.symphony.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodexAppServerClientTest {

    @TempDir
    Path tempDir;

    @Test
    void startSession_usesCompatibleDefaults() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r thread
                if [[ "$thread" != *'"approvalPolicy":"never"'* ]]; then
                  printf '%s\n' '{"id":2,"error":{"message":"unexpected approval policy"}}'
                  exit 0
                fi
                if [[ "$thread" != *'"sandbox":"workspace-write"'* ]]; then
                  printf '%s\n' '{"id":2,"error":{"message":"unexpected sandbox"}}'
                  exit 0
                fi
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-1"}}}'
                sleep 5
                """);
        CodexAppServerClient client =
                new CodexAppServerClient(script.toString(), tempDir, 2_000, 2_000, null, null, null);

        try {
            assertEquals("thread-1", client.startSession());
        } finally {
            client.stopSession();
        }
    }

    @Test
    void startSession_mapsLegacyApprovalAndSandboxValues() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r thread
                if [[ "$thread" != *'"approvalPolicy":"never"'* ]]; then
                  printf '%s\n' '{"id":2,"error":{"message":"legacy approval was not normalized"}}'
                  exit 0
                fi
                if [[ "$thread" != *'"sandbox":"danger-full-access"'* ]]; then
                  printf '%s\n' '{"id":2,"error":{"message":"legacy sandbox was not normalized"}}'
                  exit 0
                fi
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-legacy"}}}'
                sleep 5
                """);
        CodexAppServerClient client =
                new CodexAppServerClient(script.toString(), tempDir, 2_000, 2_000, "auto", "none", null);

        try {
            assertEquals("thread-legacy", client.startSession());
        } finally {
            client.stopSession();
        }
    }

    @Test
    void startSession_supportsMapApprovalPolicy() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r thread
                if [[ "$thread" != *'"approvalPolicy":{"reject":{"rules":true}}'* ]]; then
                  printf '%s\n' '{"id":2,"error":{"message":"map approval policy missing"}}'
                  exit 0
                fi
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-map"}}}'
                sleep 5
                """);
        CodexAppServerClient client = new CodexAppServerClient(
                script.toString(),
                tempDir,
                2_000,
                2_000,
                Map.of("reject", Map.of("rules", true)),
                null,
                null,
                null,
                null);

        try {
            assertEquals("thread-map", client.startSession());
        } finally {
            client.stopSession();
        }
    }

    @Test
    void startSession_surfacesThreadStartErrorMessage() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r _
                printf '%s\n' '{"id":2,"error":{"code":-32600,"message":"Invalid request"}}'
                sleep 1
                """);
        CodexAppServerClient client =
                new CodexAppServerClient(script.toString(), tempDir, 2_000, 2_000, null, null, null);

        CodexAppServerClient.CodexClientException error =
                assertThrows(CodexAppServerClient.CodexClientException.class, client::startSession);
        assertEquals("response_error", error.getCode());
        assertTrue(error.getMessage().contains("thread/start failed: Invalid request"));
        client.stopSession();
    }

    @Test
    void runTurn_surfacesTurnStartErrorMessage() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r _
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-1"}}}'
                read -r _
                printf '%s\n' '{"id":3,"error":{"message":"turn rejected"}}'
                sleep 1
                """);
        CodexAppServerClient client =
                new CodexAppServerClient(script.toString(), tempDir, 2_000, 2_000, null, null, null);

        try {
            String threadId = client.startSession();
            CodexAppServerClient.CodexClientException error = assertThrows(
                    CodexAppServerClient.CodexClientException.class, () -> client.runTurn(threadId, "hello", "title"));
            assertEquals("response_error", error.getCode());
            assertTrue(error.getMessage().contains("turn/start failed: turn rejected"));
        } finally {
            client.stopSession();
        }
    }

    @Test
    void runTurn_extractsUsageAndRateLimitsFromCurrentEventShape() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r _
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-1"}}}'
                read -r _
                printf '%s\n' '{"id":3,"result":{"turn":{"id":"turn-1"}}}'
                printf '%s\n' '{"method":"thread/tokenUsage/updated","params":{"threadId":"thread-1","turnId":"turn-1","tokenUsage":{"total":{"inputTokens":11,"outputTokens":22,"totalTokens":33},"last":{"inputTokens":1,"outputTokens":2,"totalTokens":3},"modelContextWindow":1000}}}'
                printf '%s\n' '{"method":"account/rateLimits/updated","params":{"rateLimits":{"primary":{"usedPercent":1}}}}'
                printf '%s\n' '{"method":"turn/completed","params":{"threadId":"thread-1","turn":{"id":"turn-1","status":"completed"}}}'
                sleep 1
                """);
        CodexAppServerClient client =
                new CodexAppServerClient(script.toString(), tempDir, 2_000, 2_000, null, null, null);
        List<CodexUpdateEvent> events = new ArrayList<>();
        client.addListener(events::add);

        try {
            String threadId = client.startSession();
            assertTrue(client.runTurn(threadId, "hello", "title"));
        } finally {
            client.stopSession();
        }

        assertTrue(events.stream()
                .anyMatch(event -> event.usage() != null
                        && Objects.equals(event.usage().get("input_tokens"), 11L)
                        && Objects.equals(event.usage().get("output_tokens"), 22L)
                        && Objects.equals(event.usage().get("total_tokens"), 33L)));
        assertTrue(events.stream()
                .anyMatch(event -> event.payload() != null && event.payload().containsKey("rate_limits")));
    }

    @Test
    void startSession_expandsRemoteTildeWorkspaceBeforeLaunch() throws Exception {
        Path fakeSsh = tempDir.resolve("fake-ssh.sh");
        Files.writeString(fakeSsh, """
                #!/usr/bin/env bash
                set -euo pipefail
                while [[ $# -gt 0 ]]; do
                  case "$1" in
                    -T)
                      shift
                      ;;
                    -p|-F)
                      shift 2
                      ;;
                    *)
                      if [[ -z "${destination:-}" ]]; then
                        destination="$1"
                        shift
                      else
                        break
                      fi
                      ;;
                  esac
                done
                exec bash -lc "$1"
                """);
        fakeSsh.toFile().setExecutable(true);

        Path observedCwd = tempDir.resolve("observed-cwd.txt");
        Path serverScript = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                pwd -P > %s
                read -r _
                printf '%%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r _
                printf '%%s\n' '{"id":2,"result":{"thread":{"id":"thread-remote"}}}'
                sleep 5
                """.formatted(observedCwd));

        Path expectedWorkspace = Path.of(System.getProperty("user.home"), ".codex-remote-cwd-test");
        Files.createDirectories(expectedWorkspace);

        String oldExecutable = System.getProperty("symphony.ssh.executable");
        System.setProperty("symphony.ssh.executable", fakeSsh.toString());
        CodexAppServerClient client = new CodexAppServerClient(
                serverScript.toString(),
                Path.of("~/.codex-remote-cwd-test"),
                2_000,
                2_000,
                null,
                null,
                null,
                null,
                "fake-worker");

        try {
            assertEquals("thread-remote", client.startSession());
            assertEquals(
                    expectedWorkspace.toString(), Files.readString(observedCwd).trim());
        } finally {
            client.stopSession();
            if (oldExecutable == null) {
                System.clearProperty("symphony.ssh.executable");
            } else {
                System.setProperty("symphony.ssh.executable", oldExecutable);
            }
            Files.deleteIfExists(observedCwd);
            Files.deleteIfExists(expectedWorkspace);
        }
    }

    @Test
    void startSession_advertisesDynamicTools() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r thread
                if [[ "$thread" != *'"dynamicTools"'* ]]; then
                  printf '%s\n' '{"id":2,"error":{"message":"dynamic tools missing"}}'
                  exit 0
                fi
                if [[ "$thread" != *'"name":"linear_graphql"'* ]]; then
                  printf '%s\n' '{"id":2,"error":{"message":"linear_graphql missing"}}'
                  exit 0
                fi
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-tools"}}}'
                sleep 5
                """);
        CodexAppServerClient client = new CodexAppServerClient(
                script.toString(), tempDir, 2_000, 2_000, null, null, null, simpleToolExecutor());

        try {
            assertEquals("thread-tools", client.startSession());
        } finally {
            client.stopSession();
        }
    }

    @Test
    void runTurn_handlesDynamicToolCallAndResponds() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r _
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-1"}}}'
                read -r _
                printf '%s\n' '{"id":3,"result":{"turn":{"id":"turn-1"}}}'
                printf '%s\n' '{"id":101,"method":"item/tool/call","params":{"name":"linear_graphql","arguments":{"query":"query Viewer { viewer { id } }"}}}'
                read -r tool_result
                if [[ "$tool_result" != *'"id":101'* ]]; then
                  exit 10
                fi
                if [[ "$tool_result" != *'"success":true'* ]]; then
                  exit 11
                fi
                printf '%s\n' '{"method":"turn/completed","params":{"threadId":"thread-1","turn":{"id":"turn-1","status":"completed"}}}'
                sleep 1
                """);
        CodexAppServerClient client = new CodexAppServerClient(
                script.toString(), tempDir, 2_000, 2_000, null, null, null, simpleToolExecutor());
        List<CodexUpdateEvent> events = new ArrayList<>();
        client.addListener(events::add);

        try {
            String threadId = client.startSession();
            assertTrue(client.runTurn(threadId, "hello", "title"));
        } finally {
            client.stopSession();
        }

        assertTrue(events.stream().anyMatch(event -> "tool_call_completed".equals(event.event())));
    }

    @Test
    void runTurn_autoApprovesCommandExecutionRequestsWhenPolicyIsNever() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r _
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-approve"}}}'
                read -r _
                printf '%s\n' '{"id":3,"result":{"turn":{"id":"turn-approve"}}}'
                printf '%s\n' '{"id":202,"method":"item/commandExecution/requestApproval","params":{"command":"git status"}}'
                read -r approval
                if [[ "$approval" != *'"decision":"acceptForSession"'* ]]; then
                  exit 12
                fi
                printf '%s\n' '{"method":"turn/completed","params":{"threadId":"thread-approve","turn":{"id":"turn-approve","status":"completed"}}}'
                sleep 1
                """);
        CodexAppServerClient client =
                new CodexAppServerClient(script.toString(), tempDir, 2_000, 2_000, null, null, null);

        try {
            String threadId = client.startSession();
            assertTrue(client.runTurn(threadId, "hello", "title"));
        } finally {
            client.stopSession();
        }
    }

    @Test
    void runTurn_autoAnswersToolRequestUserInput() throws Exception {
        Path script = writeScript("""
                #!/usr/bin/env bash
                set -euo pipefail
                read -r _
                printf '%s\n' '{"id":1,"result":{"userAgent":"test"}}'
                read -r _
                read -r _
                printf '%s\n' '{"id":2,"result":{"thread":{"id":"thread-input"}}}'
                read -r _
                printf '%s\n' '{"id":3,"result":{"turn":{"id":"turn-input"}}}'
                printf '%s\n' '{"id":303,"method":"item/tool/requestUserInput","params":{"questions":[{"id":"mcp_tool_call_approval_call-717","options":[{"label":"Approve Once"},{"label":"Approve this Session"},{"label":"Deny"}]}]}}'
                read -r answers
                if [[ "$answers" != *'"Approve this Session"'* ]]; then
                  exit 13
                fi
                printf '%s\n' '{"method":"turn/completed","params":{"threadId":"thread-input","turn":{"id":"turn-input","status":"completed"}}}'
                sleep 1
                """);
        CodexAppServerClient client =
                new CodexAppServerClient(script.toString(), tempDir, 2_000, 2_000, null, null, null);

        try {
            String threadId = client.startSession();
            assertTrue(client.runTurn(threadId, "hello", "title"));
        } finally {
            client.stopSession();
        }
    }

    private Path writeScript(String content) throws IOException {
        Path script = tempDir.resolve("fake-codex.sh");
        Files.writeString(script, content);
        script.toFile().setExecutable(true);
        return script;
    }

    private static CodexAppServerClient.DynamicToolExecutor simpleToolExecutor() {
        return new CodexAppServerClient.DynamicToolExecutor() {
            @Override
            public List<Map<String, Object>> toolSpecifications() {
                return List.of(Map.of(
                        "name", "linear_graphql",
                        "description", "test tool",
                        "inputSchema", Map.of("type", "object")));
            }

            @Override
            public Map<String, Object> execute(String toolName, Object arguments) {
                return Map.of(
                        "success",
                        true,
                        "output",
                        "{\"ok\":true}",
                        "contentItems",
                        List.of(Map.of("type", "inputText", "text", "{\"ok\":true}")));
            }
        };
    }
}
