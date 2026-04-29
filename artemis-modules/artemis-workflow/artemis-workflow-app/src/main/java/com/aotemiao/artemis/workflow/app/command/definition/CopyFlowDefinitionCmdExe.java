package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 复制流程定义命令执行器。 */
@Component
public class CopyFlowDefinitionCmdExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public CopyFlowDefinitionCmdExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public FlowDefinition execute(CopyFlowDefinitionCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0 || CreateFlowDefinitionCmdExe.isBlank(cmd.flowCode())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow definition copy request");
        }
        FlowDefinition source = flowDefinitionGateway
                .findById(cmd.id())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "Flow definition not found: " + cmd.id()));
        String targetTenantId =
                CreateFlowDefinitionCmdExe.isBlank(cmd.tenantId()) ? source.getTenantId() : cmd.tenantId();
        flowDefinitionGateway
                .findByFlowCodeAndTenantId(cmd.flowCode(), targetTenantId)
                .ifPresent(existing -> {
                    throw new BizException(
                            CommonErrorCode.BAD_REQUEST,
                            "Flow definition code already exists: " + existing.getFlowCode());
                });
        FlowDefinition copied = new FlowDefinition();
        copied.setFlowCode(cmd.flowCode());
        copied.setFlowName(
                CreateFlowDefinitionCmdExe.isBlank(cmd.flowName()) ? source.getFlowName() + " Copy" : cmd.flowName());
        copied.setModelType(source.getModelType());
        copied.setCategoryId(source.getCategoryId());
        copied.setVersion(Integer.valueOf(1));
        copied.setPublishStatus(CreateFlowDefinitionCmdExe.UNPUBLISHED);
        copied.setCustomForm(source.getCustomForm());
        copied.setFormPath(source.getFormPath());
        copied.setActiveStatus(CreateFlowDefinitionCmdExe.ACTIVE);
        copied.setListener(source.getListener());
        copied.setExtJson(source.getExtJson());
        copied.setTenantId(targetTenantId);
        copied.setDefinitionJson(source.getDefinitionJson());
        copied.setDefinitionXml(source.getDefinitionXml());
        return flowDefinitionGateway.save(copied);
    }
}
