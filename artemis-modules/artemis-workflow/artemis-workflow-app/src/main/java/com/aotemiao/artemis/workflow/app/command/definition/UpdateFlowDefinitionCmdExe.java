package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 修改流程定义命令执行器。 */
@Component
public class UpdateFlowDefinitionCmdExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public UpdateFlowDefinitionCmdExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public FlowDefinition execute(UpdateFlowDefinitionCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow definition id");
        }
        FlowDefinitionPayload payload = CreateFlowDefinitionCmdExe.requirePayload(cmd.payload());
        FlowDefinition existing = flowDefinitionGateway
                .findById(cmd.id())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "Flow definition not found: " + cmd.id()));
        flowDefinitionGateway
                .findByFlowCodeAndTenantId(payload.flowCode(), payload.tenantId())
                .ifPresent(found -> {
                    if (!found.getId().equals(existing.getId())) {
                        throw new BizException(
                                CommonErrorCode.BAD_REQUEST,
                                "Flow definition code already exists: " + payload.flowCode());
                    }
                });
        CreateFlowDefinitionCmdExe.fill(existing, payload);
        return flowDefinitionGateway.save(existing);
    }
}
