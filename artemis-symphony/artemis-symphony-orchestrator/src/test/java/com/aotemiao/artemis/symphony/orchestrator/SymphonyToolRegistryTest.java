package com.aotemiao.artemis.symphony.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 校验 tools/registry.json 契约，并确认其稳定错误码与运行时执行器保持同步（取代 check-symphony-assets.sh 的 registry 校验）。 */
class SymphonyToolRegistryTest {

    private static Path repoRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            if (Files.exists(dir.resolve("artemis-symphony/tools/registry.json"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("repo root with artemis-symphony/tools/registry.json not found");
    }

    @Test
    void registry_declaresLinearGraphqlContractInSyncWithExecutor() throws Exception {
        Path root = repoRoot();
        JsonNode registry =
                new ObjectMapper().readTree(Files.readString(root.resolve("artemis-symphony/tools/registry.json")));

        assertEquals(1, registry.path("schema_version").asInt(), "schema_version");
        assertEquals("symphony_tool_registry", registry.path("registry_type").asText(), "registry_type");

        JsonNode tools = registry.path("tools");
        assertTrue(tools.isArray() && tools.size() > 0, "tools must be a non-empty array");

        JsonNode linear = null;
        for (JsonNode t : tools) {
            if ("linear_graphql".equals(t.path("name").asText())) {
                linear = t;
            }
        }
        assertNotNull(linear, "linear_graphql tool entry required");

        assertEquals("linear", linear.path("provider").asText(), "linear_graphql.provider");
        assertEquals("linear", linear.path("availability").path("tracker_kind").asText(), "availability.tracker_kind");
        assertEquals(
                "external_api",
                linear.path("permissions").path("permission_level").asText(),
                "permissions.permission_level");
        assertTrue(
                linear.path("permissions").path("external_write_allowed").asBoolean(),
                "linear_graphql must declare external_write_allowed=true");

        List<String> outRequired = new ArrayList<>();
        linear.path("output_schema").path("required").forEach(n -> outRequired.add(n.asText()));
        assertTrue(
                outRequired.containsAll(List.of("success", "output", "contentItems")),
                "output_schema.required must keep the stable output shape");

        List<String> codes = new ArrayList<>();
        linear.path("failure_behavior").path("stable_error_codes").forEach(n -> codes.add(n.asText()));
        assertFalse(codes.isEmpty(), "stable_error_codes must be non-empty");
        assertEquals(codes.size(), codes.stream().distinct().count(), "stable_error_codes must be unique");
        assertTrue(
                codes.containsAll(List.of(
                        "missing_query",
                        "invalid_arguments",
                        "invalid_variables",
                        "linear_api_status",
                        "linear_api_request",
                        "tracker_graphql_unsupported")),
                "registry must declare the known linear_graphql error codes");

        // doc<->code 同步：registry 声明的稳定错误码必须出现在运行时执行器中
        String executor = Files.readString(root.resolve("artemis-symphony/artemis-symphony-orchestrator/src/main/java/"
                + "com/aotemiao/artemis/symphony/orchestrator/LinearGraphqlDynamicToolExecutor.java"));
        assertTrue(executor.contains("\"linear_graphql\""), "executor must declare linear_graphql");
        for (String code : codes) {
            assertTrue(executor.contains("\"" + code + "\""), "stable error code missing in executor: " + code);
        }
    }
}
