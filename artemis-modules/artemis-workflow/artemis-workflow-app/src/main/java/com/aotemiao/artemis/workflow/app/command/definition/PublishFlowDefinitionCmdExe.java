package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** 发布流程定义命令执行器。 */
@Component
public class PublishFlowDefinitionCmdExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public PublishFlowDefinitionCmdExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public FlowDefinition execute(ChangeFlowDefinitionStateCmd cmd) {
        FlowDefinition definition = findDefinition(cmd);
        ensureAssigneeConfigured(definition.getDefinitionJson());
        definition.setPublishStatus(CreateFlowDefinitionCmdExe.PUBLISHED);
        return flowDefinitionGateway.save(definition);
    }

    static void ensureAssigneeConfigured(String definitionJson) {
        if (CreateFlowDefinitionCmdExe.isBlank(definitionJson)) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST, "Flow definition JSON must not be blank before publish");
        }
        String normalized = definitionJson.toLowerCase(Locale.ROOT);
        if (!normalized.contains("assigneeconfig") && !normalized.contains("assignee")) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST,
                    "Flow definition intermediate nodes must configure assignees before publish");
        }
    }

    private FlowDefinition findDefinition(ChangeFlowDefinitionStateCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow definition id");
        }
        return flowDefinitionGateway
                .findById(cmd.id())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "Flow definition not found: " + cmd.id()));
    }
}
