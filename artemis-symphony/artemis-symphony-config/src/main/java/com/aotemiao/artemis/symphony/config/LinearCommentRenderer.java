package com.aotemiao.artemis.symphony.config;

import com.aotemiao.artemis.symphony.core.WorkflowErrors;
import com.aotemiao.artemis.symphony.core.model.Issue;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/** 渲染 Linear 进度评论模板；严格模式下未知变量会失败。 */
public final class LinearCommentRenderer {

    private static final PebbleEngine ENGINE =
            new PebbleEngine.Builder().strictVariables(true).build();

    private static final String DEFAULT_SUCCESS_TEMPLATE = """
            Symphony 已完成一次处理尝试。

            - 议题：{{ issue.identifier }} - {{ issue.title }}
            - 结果：{{ attempt.outcome_text }}
            - 尝试序号：{{ attempt.number }}
            - 工作区：`{{ workspace.path }}`
            - 开始：{{ attempt.started_at }}
            - 结束：{{ attempt.finished_at }}
            - 耗时（秒）：{{ attempt.duration_seconds }}
            - 已完成 turns：{{ attempt.turn_count }}
            - 最近事件：{{ attempt.last_codex_event }}
            - Token：input={{ usage.input_tokens }}, output={{ usage.output_tokens }}, total={{ usage.total_tokens }}
            {% if issue.url %}
            - Linear：{{ issue.url }}
            {% endif %}
            """;

    private static final String DEFAULT_FAILURE_TEMPLATE = """
            Symphony 本轮处理失败，已安排重试。

            - 议题：{{ issue.identifier }} - {{ issue.title }}
            - 结果：{{ attempt.outcome_text }}
            - 尝试序号：{{ attempt.number }}
            - 工作区：`{{ workspace.path }}`
            - 开始：{{ attempt.started_at }}
            - 结束：{{ attempt.finished_at }}
            - 耗时（秒）：{{ attempt.duration_seconds }}
            - 已完成 turns：{{ attempt.turn_count }}
            - 最近事件：{{ attempt.last_codex_event }}
            - Token：input={{ usage.input_tokens }}, output={{ usage.output_tokens }}, total={{ usage.total_tokens }}
            {% if retry.error %}
            - 失败原因：{{ retry.error }}
            {% endif %}
            {% if retry.scheduled %}
            - 下次重试序号：{{ retry.next_attempt }}
            - 下次重试时间：{{ retry.due_at }}
            - 重试延迟（秒）：{{ retry.delay_seconds }}
            {% endif %}
            {% if issue.url %}
            - Linear：{{ issue.url }}
            {% endif %}
            """;

    private LinearCommentRenderer() {}

    public static String renderSuccess(String template, Issue issue, Map<String, Object> context) {
        return render(template, DEFAULT_SUCCESS_TEMPLATE, issue, context);
    }

    public static String renderFailure(String template, Issue issue, Map<String, Object> context) {
        return render(template, DEFAULT_FAILURE_TEMPLATE, issue, context);
    }

    private static String render(String template, String defaultTemplate, Issue issue, Map<String, Object> context) {
        String effectiveTemplate = template != null && !template.isBlank() ? template : defaultTemplate;
        Map<String, Object> renderContext = new HashMap<>();
        renderContext.put("issue", PromptRenderer.toTemplateMap(issue));
        if (context != null && !context.isEmpty()) {
            renderContext.putAll(context);
        }
        try {
            PebbleTemplate pebbleTemplate = ENGINE.getLiteralTemplate(effectiveTemplate);
            Writer writer = new StringWriter();
            pebbleTemplate.evaluate(writer, renderContext);
            return writer.toString().trim();
        } catch (Exception e) {
            String code = e.getMessage() != null && e.getMessage().contains("strict mode")
                    ? WorkflowErrors.TEMPLATE_RENDER_ERROR
                    : WorkflowErrors.TEMPLATE_PARSE_ERROR;
            throw new CommentRenderException(code, e.getMessage(), e);
        }
    }

    public static final class CommentRenderException extends RuntimeException {
        private final String code;

        public CommentRenderException(String code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
