package com.aotemiao.artemis.symphony.tracker;

import com.aotemiao.artemis.symphony.core.model.BlockerRef;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Linear GraphQL 客户端：拉取候选议题、按 ID 刷新状态、按终态拉取等。见 SPEC 第 11 节。
 */
public class LinearTrackerClient {

    private static final int PAGE_SIZE = 50;
    private static final int REQUEST_TIMEOUT_MS = 30_000;

    private final String endpoint;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LinearTrackerClient(String endpoint, String apiKey) {
        this.endpoint = endpoint != null && !endpoint.isBlank() ? endpoint : "https://api.linear.app/graphql";
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch issues in the given active states for the configured project. Paginated.
     */
    public Result<List<Issue>> fetchCandidateIssues(String projectSlug, List<String> activeStates) {
        if (projectSlug == null || projectSlug.isBlank() || activeStates == null || activeStates.isEmpty()) {
            return Result.success(List.of());
        }
        List<Issue> all = new ArrayList<>();
        String cursor = null;
        do {
            String query = """
                    query CandidateIssues($projectSlug: String!, $first: Int!, $after: String, $stateNames: [String!]) {
                      issues(
                        first: $first
                        after: $after
                        filter: {
                          project: { slugId: { eq: $projectSlug } }
                          state: { name: { in: $stateNames } }
                        }
                        orderBy: createdAt
                      ) {
                        nodes {
                          id
                          identifier
                          title
                          description
                          priority
                          state { name }
                          branchName
                          url
                          createdAt
                          updatedAt
                          labels { nodes { name } }
                          inverseRelations { nodes { type, relatedIssue { id, identifier, state { name } } } }
                        }
                        pageInfo { hasNextPage, endCursor }
                      }
                    }
                    """;
            Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("projectSlug", projectSlug);
            variables.put("first", PAGE_SIZE);
            variables.put("stateNames", activeStates);
            if (cursor != null) {
                variables.put("after", cursor);
            } else {
                variables.put("after", (String) null);
            }
            Result<Page> res = executeQuery(query, variables);
            if (!res.isSuccess()) {
                return Result.failure(res.errorCode(), res.errorMessage());
            }
            Page page = res.value();
            if (page == null || page.issues == null) {
                break;
            }
            for (JsonNode node : page.issues) {
                Issue issue = normalizeIssue(node);
                if (issue != null) {
                    all.add(issue);
                }
            }
            cursor = page.hasNextPage ? page.endCursor : null;
        } while (cursor != null);
        return Result.success(all);
    }

    /**
     * Fetch issues in the given states (e.g. terminal states). Used for startup cleanup.
     */
    public Result<List<Issue>> fetchIssuesByStates(String projectSlug, List<String> stateNames) {
        if (projectSlug == null || projectSlug.isBlank() || stateNames == null || stateNames.isEmpty()) {
            return Result.success(List.of());
        }
        String query = """
                query IssuesByStates($projectSlug: String!, $first: Int!, $stateNames: [String!]) {
                  issues(
                    first: $first
                    filter: {
                      project: { slugId: { eq: $projectSlug } }
                      state: { name: { in: $stateNames } }
                    }
                  ) {
                    nodes { id, identifier }
                  }
                }
                """;
        Map<String, Object> variables = Map.of("projectSlug", projectSlug, "first", 250, "stateNames", stateNames);
        Result<Page> res = executeQueryForNodes(query, variables, "issues");
        if (!res.isSuccess()) {
            return Result.failure(res.errorCode(), res.errorMessage());
        }
        List<Issue> list = new ArrayList<>();
        for (JsonNode node : res.value().issues) {
            String id = node.path("id").asText(null);
            String identifier = node.path("identifier").asText(null);
            if (id != null && identifier != null) {
                list.add(new Issue(
                        id, identifier, null, null, null, null, null, null, List.of(), List.of(), null, null));
            }
        }
        return Result.success(list);
    }

    /**
     * Fetch current states for specific issue IDs (reconciliation).
     */
    public Result<List<Issue>> fetchIssueStatesByIds(List<String> issueIds) {
        if (issueIds == null || issueIds.isEmpty()) {
            return Result.success(List.of());
        }
        String query = """
                query IssueStates($ids: [ID!]!) {
                  issues(filter: { id: { in: $ids } }) {
                    nodes {
                      id
                      identifier
                      title
                      description
                      priority
                      state { name }
                      branchName
                      url
                      createdAt
                      updatedAt
                      labels { nodes { name } }
                      inverseRelations { nodes { type, relatedIssue { id, identifier, state { name } } } }
                    }
                  }
                }
                """;
        Map<String, Object> variables = Map.of("ids", issueIds);
        Result<Page> res = executeQueryForNodes(query, variables, "issues");
        if (!res.isSuccess()) {
            return Result.failure(res.errorCode(), res.errorMessage());
        }
        List<Issue> list = new ArrayList<>();
        for (JsonNode node : res.value().issues) {
            Issue issue = normalizeIssue(node);
            if (issue != null) list.add(issue);
        }
        return Result.success(list);
    }

    private Result<Page> executeQueryForNodes(String query, Map<String, Object> variables, String topLevelKey) {
        Result<JsonNode> raw = executeGraphql(query, variables);
        if (!raw.isSuccess()) {
            return Result.failure(raw.errorCode(), raw.errorMessage());
        }
        JsonNode data = raw.value().path("data");
        JsonNode keyNode = data.path(topLevelKey);
        Page page = new Page();
        JsonNode nodes = keyNode.path("nodes");
        page.issues = nodes.isArray() ? new ArrayList<>() : null;
        if (page.issues != null) {
            nodes.forEach(page.issues::add);
        }
        page.hasNextPage = keyNode.path("pageInfo").path("hasNextPage").asBoolean(false);
        page.endCursor = keyNode.path("pageInfo").path("endCursor").asText(null);
        return Result.success(page);
    }

    private Result<Page> executeQuery(String query, Map<String, Object> variables) {
        Result<JsonNode> raw = executeGraphql(query, variables);
        if (!raw.isSuccess()) {
            return Result.failure(raw.errorCode(), raw.errorMessage());
        }
        JsonNode data = raw.value().path("data");
        JsonNode issuesNode = data.path("issues");
        Page page = new Page();
        JsonNode nodes = issuesNode.path("nodes");
        page.issues = nodes.isArray() ? new ArrayList<>() : null;
        if (page.issues != null) {
            nodes.forEach(page.issues::add);
        }
        JsonNode pageInfo = issuesNode.path("pageInfo");
        page.hasNextPage = pageInfo.path("hasNextPage").asBoolean(false);
        page.endCursor = pageInfo.path("endCursor").asText(null);
        return Result.success(page);
    }

    private Result<JsonNode> executeGraphql(String query, Map<String, Object> variables) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("query", query);
            body.set("variables", objectMapper.valueToTree(variables));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", apiKey != null ? apiKey : "")
                    .timeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if (response.statusCode() != 200) {
                return Result.failure("linear_api_status", "HTTP " + response.statusCode());
            }
            if (root.has("errors")) {
                return Result.failure(
                        "linear_graphql_errors", root.get("errors").toString());
            }
            return Result.success(root);
        } catch (Exception e) {
            return Result.failure("linear_api_request", e.getMessage());
        }
    }

    private Issue normalizeIssue(JsonNode node) {
        String id = node.path("id").asText(null);
        String identifier = node.path("identifier").asText(null);
        String title = node.path("title").asText(null);
        String state = node.has("state") && node.get("state").has("name")
                ? node.get("state").get("name").asText(null)
                : null;
        if (id == null || identifier == null || title == null || state == null) {
            return null;
        }
        String description = node.path("description").asText(null);
        int priority = node.path("priority").asInt(0);
        String branchName = node.path("branchName").asText(null);
        String url = node.path("url").asText(null);
        List<String> labels = new ArrayList<>();
        if (node.has("labels") && node.get("labels").has("nodes")) {
            for (JsonNode n : node.get("labels").get("nodes")) {
                String name = n.path("name").asText(null);
                if (name != null) labels.add(name.toLowerCase());
            }
        }
        List<BlockerRef> blockedBy = new ArrayList<>();
        if (node.has("inverseRelations") && node.get("inverseRelations").has("nodes")) {
            for (JsonNode rel : node.get("inverseRelations").get("nodes")) {
                if (!"blocks".equals(rel.path("type").asText(null))) continue;
                JsonNode related = rel.path("relatedIssue");
                if (related.isMissingNode()) continue;
                blockedBy.add(new BlockerRef(
                        related.path("id").asText(null),
                        related.path("identifier").asText(null),
                        related.has("state") && related.get("state").has("name")
                                ? related.get("state").get("name").asText(null)
                                : null));
            }
        }
        Instant createdAt = parseIso(node.path("createdAt").asText(null));
        Instant updatedAt = parseIso(node.path("updatedAt").asText(null));
        return new Issue(
                id,
                identifier,
                title,
                description,
                priority <= 0 ? null : priority,
                state,
                branchName,
                url,
                List.copyOf(labels),
                List.copyOf(blockedBy),
                createdAt,
                updatedAt);
    }

    private static Instant parseIso(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Instant.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static class Page {
        List<JsonNode> issues;
        boolean hasNextPage;
        String endCursor;
    }

    public record Result<T>(boolean success, String errorCode, String errorMessage, T value) {

        public boolean isSuccess() {
            return success;
        }

        public static <T> Result<T> success(T value) {
            return new Result<>(true, null, null, value);
        }

        public static <T> Result<T> failure(String errorCode, String errorMessage) {
            return new Result<>(false, errorCode, errorMessage, null);
        }
    }
}
