package com.aotemiao.artemis.symphony.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.Issue;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LinearCommentRendererTest {

    @Test
    void renderSuccess_usesCustomTemplateContext() {
        Issue issue = new Issue(
                "issue-1",
                "ART-1",
                "进度汇报",
                "整理当前进度",
                1,
                "Todo",
                null,
                "https://linear.app/example/ART-1",
                List.of("report"),
                List.of(),
                Instant.parse("2026-03-25T01:00:00Z"),
                Instant.parse("2026-03-25T01:30:00Z"));

        String body = LinearCommentRenderer.renderSuccess(
                """
                {{ issue.identifier }}|{{ attempt.number }}|{{ workspace.path }}|{{ usage.total_tokens }}
                """,
                issue,
                Map.of(
                        "attempt",
                        Map.of("number", 3),
                        "workspace",
                        Map.of("path", "/tmp/art-1"),
                        "usage",
                        Map.of("total_tokens", 321)));

        assertTrue(body.contains("ART-1|3|/tmp/art-1|321"));
    }
}
