package com.aotemiao.artemis.symphony.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.Issue;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PromptRendererTest {

    @Test
    void render_supportsBooleanAttemptGuards() {
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

        String template = """
                {{ issue.identifier }}
                {% if has_attempt %}
                retry={{ attempt_number }}
                {% endif %}
                """;

        String firstAttempt = PromptRenderer.render(template, issue, null);
        String retryAttempt = PromptRenderer.render(template, issue, 3);

        assertTrue(firstAttempt.contains("ART-1"));
        assertFalse(firstAttempt.contains("retry="));
        assertTrue(retryAttempt.contains("retry=3"));
    }
}
