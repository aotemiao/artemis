package com.aotemiao.artemis.symphony.config;

import com.aotemiao.artemis.symphony.core.WorkflowErrors;
import com.aotemiao.artemis.symphony.core.model.Issue;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据工作流模板与议题、重试次数渲染提示词；严格模式下未知变量会失败。见 SPEC 第 5.4、12 节。
 */
public final class PromptRenderer {

    private static final PebbleEngine ENGINE =
            new PebbleEngine.Builder().strictVariables(true).build();

    private PromptRenderer() {}

    /**
     * Render prompt template with the given issue and optional attempt (null for first run).
     *
     * @return rendered prompt string
     * @throws PromptRenderException on template parse or render error (unknown variable/filter)
     */
    public static String render(String promptTemplate, Issue issue, Integer attempt) {
        if (promptTemplate == null || promptTemplate.isBlank()) {
            return """
                    You are working on a Linear issue.

                    Identifier: %s
                    Title: %s

                    Body:
                    %s
                    """.formatted(
                    issue.identifier(),
                    issue.title(),
                    issue.description() != null && !issue.description().isBlank()
                            ? issue.description()
                            : "No description provided.").trim();
        }
        Map<String, Object> context = new HashMap<>();
        context.put("issue", toTemplateMap(issue));
        context.put("has_attempt", attempt != null);
        context.put("attempt_number", attempt != null ? attempt : 0);
        if (attempt != null) {
            context.put("attempt", attempt);
        }
        try {
            PebbleTemplate template = ENGINE.getLiteralTemplate(promptTemplate);
            Writer writer = new StringWriter();
            template.evaluate(writer, context);
            return writer.toString().trim();
        } catch (Exception e) {
            String code = e.getMessage() != null && e.getMessage().contains("strict mode")
                    ? WorkflowErrors.TEMPLATE_RENDER_ERROR
                    : WorkflowErrors.TEMPLATE_PARSE_ERROR;
            throw new PromptRenderException(code, e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> toTemplateMap(Issue issue) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", issue.id());
        m.put("identifier", issue.identifier());
        m.put("title", issue.title());
        m.put("description", issue.description());
        m.put("priority", issue.priority());
        m.put("state", issue.state());
        m.put("branch_name", issue.branchName());
        m.put("url", issue.url());
        m.put("labels", issue.labels() != null ? issue.labels() : List.of());
        m.put(
                "blocked_by",
                issue.blockedBy() != null
                        ? issue.blockedBy().stream()
                                .map(b -> Map.<String, Object>of(
                                        "id", nullToEmpty(b.id()),
                                        "identifier", nullToEmpty(b.identifier()),
                                        "state", nullToEmpty(b.state())))
                                .toList()
                        : List.<Map<String, Object>>of());
        m.put("created_at", issue.createdAt());
        m.put("updated_at", issue.updatedAt());
        return m;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    public static final class PromptRenderException extends RuntimeException {
        private final String code;

        public PromptRenderException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
