package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除流程定义命令执行器。 */
@Component
public class DeleteFlowDefinitionCmdExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public DeleteFlowDefinitionCmdExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public void execute(DeleteFlowDefinitionCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow definition id");
        }
        flowDefinitionGateway
                .findById(cmd.id())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "Flow definition not found: " + cmd.id()));
        if (flowDefinitionGateway.existsUsedByInstance(cmd.id())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow definition has been used by instances");
        }
        flowDefinitionGateway.deleteById(cmd.id());
    }
}
