package com.aotemiao.artemis.symphony.tracker;

import com.aotemiao.artemis.symphony.core.model.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/** tracker 抽象边界：协调层只依赖该接口，而不是具体的 Linear 客户端。 */
public interface TrackerClient {

    TrackerResult<List<Issue>> fetchCandidateIssues(String projectSlug, List<String> activeStates);

    TrackerResult<List<Issue>> fetchIssuesByStates(String projectSlug, List<String> stateNames);

    TrackerResult<List<Issue>> fetchIssueStatesByIds(List<String> issueIds);

    TrackerResult<String> createIssueComment(String issueId, String body);

    TrackerResult<Void> updateIssueState(String issueId, String stateName);

    default TrackerResult<JsonNode> executeGraphql(String query, Map<String, Object> variables) {
        return TrackerResult.failure("tracker_graphql_unsupported", "当前 tracker 不支持 GraphQL 动态工具");
    }
}
