package com.aotemiao.artemis.symphony.config;

import com.aotemiao.artemis.symphony.core.WorkflowErrors;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Reads WORKFLOW.md, parses YAML front matter and prompt body. SPEC Section 5.1, 5.2.
 */
public final class WorkflowLoader {

    private static final String FRONT_MATTER_START = "---";
    private static final String FRONT_MATTER_END = "---";

    private WorkflowLoader() {}

    /**
     * Load workflow from the given path.
     *
     * @param workflowPath path to WORKFLOW.md (or explicit path)
     * @return Success with definition or Error with code and message
     */
    public static WorkflowLoadResult load(Path workflowPath) {
        if (workflowPath == null || !Files.isRegularFile(workflowPath)) {
            return new WorkflowLoadResult.Error(
                    WorkflowErrors.MISSING_WORKFLOW_FILE,
                    "Workflow file not found: " + workflowPath);
        }
        try {
            String content = Files.readString(workflowPath);
            return parse(content);
        } catch (Exception e) {
            return new WorkflowLoadResult.Error(
                    WorkflowErrors.WORKFLOW_PARSE_ERROR,
                    e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    /**
     * Parse workflow content: optional YAML front matter + prompt body.
     */
    @SuppressWarnings("unchecked")
    public static WorkflowLoadResult parse(String content) {
        if (content == null) {
            content = "";
        }
        String trimmed = content.stripLeading();
        Map<String, Object> config;
        String promptTemplate;

        if (trimmed.startsWith(FRONT_MATTER_START)) {
            int firstEnd = trimmed.indexOf(FRONT_MATTER_END, FRONT_MATTER_START.length());
            if (firstEnd == -1) {
                return new WorkflowLoadResult.Error(
                        WorkflowErrors.WORKFLOW_PARSE_ERROR,
                        "Unclosed YAML front matter (missing closing ---)");
            }
            String yamlBlock = trimmed.substring(FRONT_MATTER_START.length(), firstEnd).trim();
            String afterFrontMatter = trimmed.substring(firstEnd + FRONT_MATTER_END.length()).stripLeading();

            if (yamlBlock.isEmpty()) {
                config = Map.of();
            } else {
                try {
                    Yaml yaml = new Yaml();
                    Object loaded = yaml.load(yamlBlock);
                    if (loaded == null) {
                        config = Map.of();
                    } else if (loaded instanceof Map<?, ?> m) {
                        config = (Map<String, Object>) m;
                    } else {
                        return new WorkflowLoadResult.Error(
                                WorkflowErrors.WORKFLOW_FRONT_MATTER_NOT_A_MAP,
                                "YAML front matter must be a map/object");
                    }
                } catch (Exception e) {
                    return new WorkflowLoadResult.Error(
                            WorkflowErrors.WORKFLOW_PARSE_ERROR,
                            e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                }
            }
            promptTemplate = afterFrontMatter.trim();
        } else {
            config = Map.of();
            promptTemplate = trimmed.trim();
        }

        return new WorkflowLoadResult.Success(new WorkflowDefinition(config, promptTemplate));
    }
}
