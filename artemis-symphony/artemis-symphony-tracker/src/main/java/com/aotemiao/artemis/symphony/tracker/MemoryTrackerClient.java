package com.aotemiao.artemis.symphony.tracker;

import com.aotemiao.artemis.symphony.core.model.Issue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 内存 tracker：用于测试和本地无外部依赖场景，对齐参考实现的 memory adapter。 */
public class MemoryTrackerClient implements TrackerClient {

    private final Map<String, Issue> issuesById = new ConcurrentHashMap<>();
    private final Map<String, List<String>> commentsByIssueId = new ConcurrentHashMap<>();

    public MemoryTrackerClient(List<Issue> initialIssues) {
        if (initialIssues != null) {
            for (Issue issue : initialIssues) {
                if (issue != null && issue.id() != null && !issue.id().isBlank()) {
                    issuesById.put(issue.id(), issue);
                }
            }
        }
    }

    @Override
    public TrackerResult<List<Issue>> fetchCandidateIssues(String projectSlug, List<String> activeStates) {
        return TrackerResult.success(filterByStates(activeStates));
    }

    @Override
    public TrackerResult<List<Issue>> fetchIssuesByStates(String projectSlug, List<String> stateNames) {
        return TrackerResult.success(filterByStates(stateNames));
    }

    @Override
    public TrackerResult<List<Issue>> fetchIssueStatesByIds(List<String> issueIds) {
        if (issueIds == null || issueIds.isEmpty()) {
            return TrackerResult.success(List.of());
        }
        List<Issue> issues = new ArrayList<>();
        for (String issueId : issueIds) {
            Issue issue = issuesById.get(issueId);
            if (issue != null) {
                issues.add(issue);
            }
        }
        return TrackerResult.success(List.copyOf(issues));
    }

    @Override
    public TrackerResult<String> createIssueComment(String issueId, String body) {
        if (issueId == null || issueId.isBlank()) {
            return TrackerResult.failure("memory_comment_issue_id", "issueId is required");
        }
        if (body == null || body.isBlank()) {
            return TrackerResult.failure("memory_comment_body", "body is required");
        }
        commentsByIssueId.computeIfAbsent(issueId, ignored -> new ArrayList<>()).add(body);
        return TrackerResult.success(
                "memory-comment-" + commentsByIssueId.get(issueId).size());
    }

    @Override
    public TrackerResult<Void> updateIssueState(String issueId, String stateName) {
        if (issueId == null || issueId.isBlank()) {
            return TrackerResult.failure("memory_issue_update_issue_id", "issueId is required");
        }
        if (stateName == null || stateName.isBlank()) {
            return TrackerResult.failure("memory_issue_update_state", "stateName is required");
        }
        Issue issue = issuesById.get(issueId);
        if (issue == null) {
            return TrackerResult.failure("memory_issue_not_found", "issue not found");
        }
        issuesById.put(issueId, withState(issue, stateName));
        return TrackerResult.success(null);
    }

    public List<String> commentsForIssue(String issueId) {
        return List.copyOf(commentsByIssueId.getOrDefault(issueId, List.of()));
    }

    public Map<String, Issue> snapshotIssues() {
        return Map.copyOf(new LinkedHashMap<>(issuesById));
    }

    private List<Issue> filterByStates(List<String> stateNames) {
        if (stateNames == null || stateNames.isEmpty()) {
            return List.copyOf(issuesById.values());
        }
        List<String> normalizedStates =
                stateNames.stream().map(MemoryTrackerClient::normalizeState).toList();
        return issuesById.values().stream()
                .filter(issue -> normalizedStates.contains(normalizeState(issue.state())))
                .toList();
    }

    private static Issue withState(Issue issue, String stateName) {
        return new Issue(
                issue.id(),
                issue.identifier(),
                issue.title(),
                issue.description(),
                issue.priority(),
                stateName,
                issue.branchName(),
                issue.url(),
                issue.assigneeId(),
                issue.labels(),
                issue.blockedBy(),
                issue.assignedToWorker(),
                issue.createdAt(),
                Instant.now());
    }

    private static String normalizeState(String stateName) {
        return stateName == null ? "" : stateName.trim().toLowerCase();
    }
}
