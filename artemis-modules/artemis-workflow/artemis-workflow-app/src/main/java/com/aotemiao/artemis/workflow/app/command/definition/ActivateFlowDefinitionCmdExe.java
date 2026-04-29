package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 激活流程定义命令执行器。 */
@Component
public class ActivateFlowDefinitionCmdExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public ActivateFlowDefinitionCmdExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public FlowDefinition execute(ChangeFlowDefinitionStateCmd cmd) {
        FlowDefinition definition = findDefinition(cmd);
        definition.setActiveStatus(CreateFlowDefinitionCmdExe.ACTIVE);
        return flowDefinitionGateway.save(definition);
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
