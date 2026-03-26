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
import java.util.Set;

/**
 * Linear GraphQL 客户端：拉取候选议题、按 ID 刷新状态、按终态拉取等。见 SPEC 第 11 节。
 */
public class LinearTrackerClient implements TrackerClient {

    private static final int PAGE_SIZE = 50;
    private static final int REQUEST_TIMEOUT_MS = 30_000;

    private final String endpoint;
    private final String apiKey;
    private final String assignee;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LinearTrackerClient(String endpoint, String apiKey) {
        this(endpoint, apiKey, null);
    }

    public LinearTrackerClient(String endpoint, String apiKey, String assignee) {
        this.endpoint = endpoint != null && !endpoint.isBlank() ? endpoint : "https://api.linear.app/graphql";
        this.apiKey = apiKey;
        this.assignee = assignee;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(REQUEST_TIMEOUT_MS))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch issues in the given active states for the configured project. Paginated.
     */
    @Override
    public TrackerResult<List<Issue>> fetchCandidateIssues(String projectSlug, List<String> activeStates) {
        if (projectSlug == null || projectSlug.isBlank() || activeStates == null || activeStates.isEmpty()) {
            return TrackerResult.success(List.of());
        }
        TrackerResult<Set<String>> assigneeFilterResult = resolveAssigneeMatchValues();
        if (!assigneeFilterResult.isSuccess()) {
            return TrackerResult.failure(assigneeFilterResult.errorCode(), assigneeFilterResult.errorMessage());
        }
        Set<String> assigneeMatchValues = assigneeFilterResult.value();
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
                          assignee { id }
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
            TrackerResult<Page> res = executeQuery(query, variables);
            if (!res.isSuccess()) {
                return TrackerResult.failure(res.errorCode(), res.errorMessage());
            }
            Page page = res.value();
            if (page == null || page.issues == null) {
                break;
            }
            for (JsonNode node : page.issues) {
                Issue issue = normalizeIssue(node, assigneeMatchValues);
                if (issue != null) {
                    all.add(issue);
                }
            }
            cursor = page.hasNextPage ? page.endCursor : null;
        } while (cursor != null);
        return TrackerResult.success(all);
    }

    /**
     * Fetch issues in the given states (e.g. terminal states). Used for startup cleanup.
     */
    @Override
    public TrackerResult<List<Issue>> fetchIssuesByStates(String projectSlug, List<String> stateNames) {
        if (projectSlug == null || projectSlug.isBlank() || stateNames == null || stateNames.isEmpty()) {
            return TrackerResult.success(List.of());
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
        TrackerResult<Page> res = executeQueryForNodes(query, variables, "issues");
        if (!res.isSuccess()) {
            return TrackerResult.failure(res.errorCode(), res.errorMessage());
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
        return TrackerResult.success(list);
    }

    /**
     * Fetch current states for specific issue IDs (reconciliation).
     */
    @Override
    public TrackerResult<List<Issue>> fetchIssueStatesByIds(List<String> issueIds) {
        if (issueIds == null || issueIds.isEmpty()) {
            return TrackerResult.success(List.of());
        }
        TrackerResult<Set<String>> assigneeFilterResult = resolveAssigneeMatchValues();
        if (!assigneeFilterResult.isSuccess()) {
            return TrackerResult.failure(assigneeFilterResult.errorCode(), assigneeFilterResult.errorMessage());
        }
        Set<String> assigneeMatchValues = assigneeFilterResult.value();
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
                      assignee { id }
                      createdAt
                      updatedAt
                      labels { nodes { name } }
                      inverseRelations { nodes { type, relatedIssue { id, identifier, state { name } } } }
                    }
                  }
                }
                """;
        Map<String, Object> variables = Map.of("ids", issueIds);
        TrackerResult<Page> res = executeQueryForNodes(query, variables, "issues");
        if (!res.isSuccess()) {
            return TrackerResult.failure(res.errorCode(), res.errorMessage());
        }
        List<Issue> list = new ArrayList<>();
        for (JsonNode node : res.value().issues) {
            Issue issue = normalizeIssue(node, assigneeMatchValues);
            if (issue != null) list.add(issue);
        }
        return TrackerResult.success(list);
    }

    /** 在指定 issue 下创建一条评论。 */
    @Override
    public TrackerResult<String> createIssueComment(String issueId, String body) {
        if (issueId == null || issueId.isBlank()) {
            return TrackerResult.failure("linear_comment_issue_id", "issueId is required");
        }
        if (body == null || body.isBlank()) {
            return TrackerResult.failure("linear_comment_body", "body is required");
        }
        String query = """
                mutation CommentCreate($issueId: String!, $body: String!) {
                  commentCreate(input: { issueId: $issueId, body: $body }) {
                    success
                    comment { id }
                  }
                }
                """;
        Map<String, Object> variables = Map.of("issueId", issueId, "body", body);
        TrackerResult<JsonNode> raw = executeGraphql(query, variables);
        if (!raw.isSuccess()) {
            return TrackerResult.failure(raw.errorCode(), raw.errorMessage());
        }
        JsonNode payload = raw.value().path("data").path("commentCreate");
        if (!payload.path("success").asBoolean(false)) {
            return TrackerResult.failure("linear_comment_create_failed", "commentCreate returned success=false");
        }
        String commentId = payload.path("comment").path("id").asText(null);
        return TrackerResult.success(commentId != null ? commentId : "");
    }

    @Override
    public TrackerResult<Void> updateIssueState(String issueId, String stateName) {
        if (issueId == null || issueId.isBlank()) {
            return TrackerResult.failure("linear_issue_update_issue_id", "issueId is required");
        }
        if (stateName == null || stateName.isBlank()) {
            return TrackerResult.failure("linear_issue_update_state", "stateName is required");
        }
        String resolveStateQuery = """
                query ResolveIssueState($issueId: String!, $stateName: String!) {
                  issue(id: $issueId) {
                    team {
                      states(filter: { name: { eq: $stateName } }, first: 1) {
                        nodes { id }
                      }
                    }
                  }
                }
                """;
        TrackerResult<JsonNode> stateLookup =
                executeGraphql(resolveStateQuery, Map.of("issueId", issueId, "stateName", stateName));
        if (!stateLookup.isSuccess()) {
            return TrackerResult.failure(stateLookup.errorCode(), stateLookup.errorMessage());
        }
        String stateId = stateLookup
                .value()
                .path("data")
                .path("issue")
                .path("team")
                .path("states")
                .path("nodes")
                .path(0)
                .path("id")
                .asText(null);
        if (stateId == null || stateId.isBlank()) {
            return TrackerResult.failure("linear_issue_update_state_not_found", "stateId not found for " + stateName);
        }
        String mutation = """
                mutation UpdateIssueState($issueId: String!, $stateId: String!) {
                  issueUpdate(id: $issueId, input: { stateId: $stateId }) {
                    success
                  }
                }
                """;
        TrackerResult<JsonNode> update = executeGraphql(mutation, Map.of("issueId", issueId, "stateId", stateId));
        if (!update.isSuccess()) {
            return TrackerResult.failure(update.errorCode(), update.errorMessage());
        }
        boolean success =
                update.value().path("data").path("issueUpdate").path("success").asBoolean(false);
        if (!success) {
            return TrackerResult.failure("linear_issue_update_failed", "issueUpdate returned success=false");
        }
        return TrackerResult.success(null);
    }

    private TrackerResult<Page> executeQueryForNodes(String query, Map<String, Object> variables, String topLevelKey) {
        TrackerResult<JsonNode> raw = executeGraphql(query, variables);
        if (!raw.isSuccess()) {
            return TrackerResult.failure(raw.errorCode(), raw.errorMessage());
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
        return TrackerResult.success(page);
    }

    private TrackerResult<Page> executeQuery(String query, Map<String, Object> variables) {
        TrackerResult<JsonNode> raw = executeGraphql(query, variables);
        if (!raw.isSuccess()) {
            return TrackerResult.failure(raw.errorCode(), raw.errorMessage());
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
        return TrackerResult.success(page);
    }

    @Override
    public TrackerResult<JsonNode> executeGraphql(String query, Map<String, Object> variables) {
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
                return TrackerResult.failure("linear_api_status", "HTTP " + response.statusCode());
            }
            if (root.has("errors")) {
                return TrackerResult.failure(
                        "linear_graphql_errors", root.get("errors").toString());
            }
            return TrackerResult.success(root);
        } catch (Exception e) {
            return TrackerResult.failure("linear_api_request", e.getMessage());
        }
    }

    private Issue normalizeIssue(JsonNode node, Set<String> assigneeMatchValues) {
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
        JsonNode assigneeNode = node.path("assignee");
        String assigneeId = assigneeNode.isObject() ? assigneeNode.path("id").asText(null) : null;
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
        boolean assignedToWorker =
                assigneeMatchValues == null || (assigneeId != null && assigneeMatchValues.contains(assigneeId));
        return new Issue(
                id,
                identifier,
                title,
                description,
                priority <= 0 ? null : priority,
                state,
                branchName,
                url,
                assigneeId,
                List.copyOf(labels),
                List.copyOf(blockedBy),
                assignedToWorker,
                createdAt,
                updatedAt);
    }

    private TrackerResult<Set<String>> resolveAssigneeMatchValues() {
        String normalizedAssignee = normalizeAssigneeValue(assignee);
        if (normalizedAssignee == null) {
            return TrackerResult.success(null);
        }
        if ("me".equals(normalizedAssignee)) {
            String query = """
                    query SymphonyLinearViewer {
                      viewer { id }
                    }
                    """;
            TrackerResult<JsonNode> viewerResult = executeGraphql(query, Map.of());
            if (!viewerResult.isSuccess()) {
                return TrackerResult.failure(viewerResult.errorCode(), viewerResult.errorMessage());
            }
            String viewerId =
                    viewerResult.value().path("data").path("viewer").path("id").asText(null);
            if (viewerId == null || viewerId.isBlank()) {
                return TrackerResult.failure("missing_linear_viewer_identity", "viewer.id is missing");
            }
            return TrackerResult.success(Set.of(viewerId));
        }
        return TrackerResult.success(Set.of(normalizedAssignee));
    }

    private static String normalizeAssigneeValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
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
}
