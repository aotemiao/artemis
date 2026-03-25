package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.agent.CodexAppServerClient;
import com.aotemiao.artemis.symphony.tracker.TrackerClient;
import com.aotemiao.artemis.symphony.tracker.TrackerResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** 参考实现中的 `linear_graphql` 动态工具：让 Codex 能复用 Symphony 的 Linear 鉴权。 */
public class LinearGraphqlDynamicToolExecutor implements CodexAppServerClient.DynamicToolExecutor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TOOL_NAME = "linear_graphql";
    private static final String TOOL_DESCRIPTION = "Execute a raw GraphQL query or mutation against Linear using Symphony's configured auth.";
    private static final Map<String, Object> TOOL_INPUT_SCHEMA = Map.of(
            "type",
            "object",
            "additionalProperties",
            false,
            "required",
            List.of("query"),
            "properties",
            Map.of(
                    "query",
                    Map.of(
                            "type", "string",
                            "description", "GraphQL query or mutation document to execute against Linear."),
                    "variables",
                    Map.of(
                            "type", List.of("object", "null"),
                            "description", "Optional GraphQL variables object.",
                            "additionalProperties", true)));

    private final Supplier<TrackerClient> trackerSupplier;

    public LinearGraphqlDynamicToolExecutor(Supplier<TrackerClient> trackerSupplier) {
        this.trackerSupplier = trackerSupplier;
    }

    @Override
    public List<Map<String, Object>> toolSpecifications() {
        return List.of(Map.of(
                "name", TOOL_NAME,
                "description", TOOL_DESCRIPTION,
                "inputSchema", TOOL_INPUT_SCHEMA));
    }

    @Override
    public Map<String, Object> execute(String toolName, Object arguments) {
        if (!TOOL_NAME.equals(toolName)) {
            return failureResponse(Map.of(
                    "error", Map.of("message", "Unsupported dynamic tool: " + String.valueOf(toolName) + ".")));
        }
        NormalizedArguments normalized = normalizeArguments(arguments);
        if (normalized.errorCode != null) {
            return failureResponse(toolErrorPayload(normalized.errorCode, null));
        }
        TrackerResult<com.fasterxml.jackson.databind.JsonNode> result =
                trackerSupplier.get().executeGraphql(normalized.query, normalized.variables);
        if (!result.isSuccess()) {
            return failureResponse(toolErrorPayload(result.errorCode(), result.errorMessage()));
        }
        return successResponse(json(result.value()));
    }

    private static NormalizedArguments normalizeArguments(Object arguments) {
        if (arguments instanceof String query) {
            String trimmed = query.trim();
            return trimmed.isEmpty()
                    ? new NormalizedArguments(null, Map.of(), "missing_query")
                    : new NormalizedArguments(trimmed, Map.of(), null);
        }
        if (!(arguments instanceof Map<?, ?> map)) {
            return new NormalizedArguments(null, Map.of(), "invalid_arguments");
        }
        Object queryRaw = map.get("query");
        if (!(queryRaw instanceof String query) || query.trim().isEmpty()) {
            return new NormalizedArguments(null, Map.of(), "missing_query");
        }
        Object variablesRaw = map.containsKey("variables") ? map.get("variables") : Map.of();
        if (!(variablesRaw instanceof Map<?, ?> variablesMap)) {
            if (variablesRaw == null) {
                return new NormalizedArguments(query.trim(), Map.of(), null);
            }
            return new NormalizedArguments(null, Map.of(), "invalid_variables");
        }
        Map<String, Object> variables = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : variablesMap.entrySet()) {
            variables.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return new NormalizedArguments(query.trim(), Map.copyOf(variables), null);
    }

    private static Map<String, Object> toolErrorPayload(String errorCode, String reason) {
        return switch (errorCode) {
            case "missing_query" -> Map.of(
                    "error", Map.of("message", "`linear_graphql` requires a non-empty `query` string."));
            case "invalid_arguments" -> Map.of(
                    "error",
                    Map.of(
                            "message",
                            "`linear_graphql` expects either a GraphQL query string or an object with `query` and optional `variables`."));
            case "invalid_variables" -> Map.of(
                    "error", Map.of("message", "`linear_graphql.variables` must be a JSON object when provided."));
            case "linear_api_status" -> Map.of(
                    "error",
                    Map.of(
                            "message", "Linear GraphQL request failed with HTTP " + safeReason(reason) + ".",
                            "status", safeReason(reason)));
            case "linear_api_request" -> Map.of(
                    "error",
                    Map.of(
                            "message", "Linear GraphQL request failed before receiving a successful response.",
                            "reason", safeReason(reason)));
            case "tracker_graphql_unsupported" -> Map.of(
                    "error",
                    Map.of("message", "Symphony 当前 tracker 未启用 Linear GraphQL 动态工具。"));
            default -> Map.of(
                    "error",
                    Map.of(
                            "message", "Linear GraphQL tool execution failed.",
                            "reason", safeReason(reason != null ? reason : errorCode)));
        };
    }

    private static Map<String, Object> successResponse(String output) {
        return Map.of(
                "success",
                true,
                "output",
                output,
                "contentItems",
                List.of(Map.of("type", "inputText", "text", output)));
    }

    private static Map<String, Object> failureResponse(Map<String, Object> payload) {
        String output = json(payload);
        return Map.of(
                "success",
                false,
                "output",
                output,
                "contentItems",
                List.of(Map.of("type", "inputText", "text", output)));
    }

    private static String json(Object payload) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }

    private static String safeReason(String reason) {
        return reason == null || reason.isBlank() ? "unknown" : reason;
    }

    private record NormalizedArguments(String query, Map<String, Object> variables, String errorCode) {}
}
