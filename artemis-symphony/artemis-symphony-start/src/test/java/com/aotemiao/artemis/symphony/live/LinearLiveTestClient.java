package com.aotemiao.artemis.symphony.live;

import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import com.aotemiao.artemis.symphony.tracker.TrackerResult;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Live e2e 专用的 Linear GraphQL helper。 */
final class LinearLiveTestClient {

    private static final int GRAPHQL_MAX_ATTEMPTS = 5;
    private static final long GRAPHQL_RETRY_DELAY_MS = 1_000L;

    private static final String TEAM_QUERY = """
            query SymphonyLiveE2ETeam($key: String!) {
              teams(filter: {key: {eq: $key}}, first: 1) {
                nodes {
                  id
                  key
                  name
                  states(first: 50) {
                    nodes {
                      id
                      name
                      type
                    }
                  }
                }
              }
            }
            """;

    private static final String CREATE_PROJECT_MUTATION = """
            mutation SymphonyLiveE2ECreateProject($name: String!, $teamIds: [String!]!) {
              projectCreate(input: {name: $name, teamIds: $teamIds}) {
                success
                project {
                  id
                  name
                  slugId
                  url
                }
              }
            }
            """;

    private static final String CREATE_ISSUE_MUTATION = """
            mutation SymphonyLiveE2ECreateIssue(
              $teamId: String!
              $projectId: String!
              $title: String!
              $description: String!
              $stateId: String
            ) {
              issueCreate(
                input: {
                  teamId: $teamId
                  projectId: $projectId
                  title: $title
                  description: $description
                  stateId: $stateId
                }
              ) {
                success
                issue {
                  id
                  identifier
                  title
                  description
                  url
                  state {
                    name
                  }
                }
              }
            }
            """;

    private static final String PROJECT_STATUSES_QUERY = """
            query SymphonyLiveE2EProjectStatuses {
              projectStatuses(first: 50) {
                nodes {
                  id
                  name
                  type
                }
              }
            }
            """;

    private static final String ISSUE_DETAILS_QUERY = """
            query SymphonyLiveE2EIssueDetails($id: String!) {
              issue(id: $id) {
                id
                identifier
                state {
                  name
                  type
                }
                comments(first: 20) {
                  nodes {
                    body
                  }
                }
              }
            }
            """;

    private static final String COMPLETE_PROJECT_MUTATION = """
            mutation SymphonyLiveE2ECompleteProject($id: String!, $statusId: String!, $completedAt: DateTime!) {
              projectUpdate(id: $id, input: {statusId: $statusId, completedAt: $completedAt}) {
                success
              }
            }
            """;

    private final LinearTrackerClient client;

    LinearLiveTestClient(String apiKey) {
        this.client = new LinearTrackerClient("https://api.linear.app/graphql", apiKey);
    }

    Team fetchTeam(String teamKey) {
        JsonNode nodes = data(TEAM_QUERY, Map.of("key", teamKey)).path("teams").path("nodes");
        if (!nodes.isArray() || nodes.isEmpty()) {
            throw new IllegalStateException("expected Linear team " + teamKey + " to exist");
        }
        JsonNode team = nodes.get(0);
        List<WorkflowState> states = new ArrayList<>();
        JsonNode stateNodes = team.path("states").path("nodes");
        if (stateNodes.isArray()) {
            for (JsonNode state : stateNodes) {
                states.add(new WorkflowState(
                        requiredText(state, "id"), requiredText(state, "name"), requiredText(state, "type")));
            }
        }
        return new Team(requiredText(team, "id"), requiredText(team, "key"), requiredText(team, "name"), states);
    }

    ProjectStatus completedProjectStatus() {
        JsonNode nodes =
                data(PROJECT_STATUSES_QUERY, Map.of()).path("projectStatuses").path("nodes");
        if (!nodes.isArray()) {
            throw new IllegalStateException("expected project statuses list");
        }
        for (JsonNode node : nodes) {
            if ("completed".equals(node.path("type").asText(null))) {
                return new ProjectStatus(
                        requiredText(node, "id"), requiredText(node, "name"), requiredText(node, "type"));
            }
        }
        throw new IllegalStateException("expected Linear workspace to expose a completed project status");
    }

    Project createProject(String teamId, String name) {
        JsonNode payload = successfulMutation(
                CREATE_PROJECT_MUTATION, Map.of("teamIds", List.of(teamId), "name", name), "projectCreate", "project");
        return new Project(
                requiredText(payload, "id"),
                requiredText(payload, "name"),
                requiredText(payload, "slugId"),
                requiredText(payload, "url"));
    }

    Issue createIssue(String teamId, String projectId, String stateId, String title) {
        JsonNode payload = successfulMutation(
                CREATE_ISSUE_MUTATION,
                Map.of(
                        "teamId", teamId,
                        "projectId", projectId,
                        "title", title,
                        "description", title,
                        "stateId", stateId),
                "issueCreate",
                "issue");
        return new Issue(
                requiredText(payload, "id"),
                requiredText(payload, "identifier"),
                requiredText(payload, "title"),
                payload.path("description").asText(null),
                null,
                payload.path("state").path("name").asText(null),
                null,
                payload.path("url").asText(null),
                List.of(),
                List.of(),
                null,
                null);
    }

    IssueDetails fetchIssueDetails(String issueId) {
        JsonNode issue = data(ISSUE_DETAILS_QUERY, Map.of("id", issueId)).path("issue");
        if (!issue.isObject()) {
            throw new IllegalStateException("expected issue details payload for " + issueId);
        }
        List<String> comments = new ArrayList<>();
        JsonNode commentNodes = issue.path("comments").path("nodes");
        if (commentNodes.isArray()) {
            for (JsonNode comment : commentNodes) {
                comments.add(comment.path("body").asText(""));
            }
        }
        String stateType = issue.path("state").path("type").asText("");
        return new IssueDetails(issue.path("identifier").asText(""), stateType, List.copyOf(comments));
    }

    void completeProject(String projectId, String statusId) {
        graphql(
                COMPLETE_PROJECT_MUTATION,
                Map.of(
                        "id",
                        projectId,
                        "statusId",
                        statusId,
                        "completedAt",
                        java.time.Instant.now()
                                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
                                .toString()));
    }

    private JsonNode data(String query, Map<String, Object> variables) {
        JsonNode root = graphql(query, variables);
        JsonNode data = root.path("data");
        if (!data.isObject()) {
            throw new IllegalStateException("expected GraphQL data payload, got: " + root);
        }
        return data;
    }

    private JsonNode successfulMutation(
            String query, Map<String, Object> variables, String mutationName, String entityName) {
        JsonNode payload = data(query, variables).path(mutationName);
        if (!payload.path("success").asBoolean(false)) {
            throw new IllegalStateException("expected successful " + mutationName + " response, got: " + payload);
        }
        JsonNode entity = payload.path(entityName);
        if (!entity.isObject()) {
            throw new IllegalStateException("expected mutation entity " + entityName + ", got: " + payload);
        }
        return entity;
    }

    private JsonNode graphql(String query, Map<String, Object> variables) {
        TrackerResult<JsonNode> lastResult = null;
        for (int attempt = 1; attempt <= GRAPHQL_MAX_ATTEMPTS; attempt++) {
            TrackerResult<JsonNode> result = client.executeGraphql(query, variables);
            if (result.isSuccess() && result.value() != null) {
                return result.value();
            }
            lastResult = result;
            if (attempt >= GRAPHQL_MAX_ATTEMPTS || !isRetryableGraphqlFailure(result)) {
                break;
            }
            sleepBeforeRetry();
        }
        throw new IllegalStateException("Linear GraphQL request failed: "
                + (lastResult != null && lastResult.errorCode() != null ? lastResult.errorCode() : "unknown")
                + " / "
                + (lastResult != null && lastResult.errorMessage() != null ? lastResult.errorMessage() : "no detail"));
    }

    private static boolean isRetryableGraphqlFailure(TrackerResult<JsonNode> result) {
        String errorCode = result != null ? result.errorCode() : null;
        String errorMessage = result != null ? result.errorMessage() : null;
        if ("linear_api_request".equals(errorCode)) {
            return true;
        }
        if (errorMessage == null || errorMessage.isBlank()) {
            return false;
        }
        String normalized = errorMessage.toLowerCase();
        return normalized.contains("eof")
                || normalized.contains("timed out")
                || normalized.contains("connection reset")
                || normalized.contains("connection refused")
                || normalized.contains("connection closed");
    }

    private static void sleepBeforeRetry() {
        try {
            Thread.sleep(GRAPHQL_RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while retrying Linear GraphQL request", e);
        }
    }

    private static String requiredText(JsonNode node, String fieldName) {
        String value = node.path(fieldName).asText(null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("expected non-empty field " + fieldName + " in payload: " + node);
        }
        return value;
    }

    record WorkflowState(String id, String name, String type) {}

    record Team(String id, String key, String name, List<WorkflowState> states) {

        WorkflowState activeState() {
            return states.stream()
                    .filter(state -> "started".equals(state.type()))
                    .findFirst()
                    .or(() -> states.stream()
                            .filter(state -> "unstarted".equals(state.type()))
                            .findFirst())
                    .or(() -> states.stream()
                            .filter(state -> !"completed".equals(state.type()) && !"canceled".equals(state.type()))
                            .findFirst())
                    .orElseThrow(
                            () -> new IllegalStateException("expected team to expose a non-terminal workflow state"));
        }

        List<String> activeStateNames() {
            List<String> active = states.stream()
                    .filter(state -> !"completed".equals(state.type()) && !"canceled".equals(state.type()))
                    .map(WorkflowState::name)
                    .toList();
            if (active.isEmpty()) {
                return List.of("Todo", "In Progress", "In Review");
            }
            return active;
        }

        List<String> terminalStateNames() {
            List<String> terminal = states.stream()
                    .filter(state -> "completed".equals(state.type()) || "canceled".equals(state.type()))
                    .map(WorkflowState::name)
                    .toList();
            if (terminal.isEmpty()) {
                return List.of("Done", "Canceled", "Cancelled");
            }
            return terminal;
        }
    }

    record Project(String id, String name, String slugId, String url) {}

    record ProjectStatus(String id, String name, String type) {}

    record IssueDetails(String identifier, String stateType, List<String> comments) {

        boolean completed() {
            return "completed".equals(stateType) || "canceled".equals(stateType);
        }
    }
}
