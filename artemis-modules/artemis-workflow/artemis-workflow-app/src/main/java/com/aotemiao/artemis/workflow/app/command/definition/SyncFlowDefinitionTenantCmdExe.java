package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 同步流程定义到租户命令执行器。 */
@Component
public class SyncFlowDefinitionTenantCmdExe {

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public SyncFlowDefinitionTenantCmdExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public FlowDefinition execute(SyncFlowDefinitionTenantCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0 || CreateFlowDefinitionCmdExe.isBlank(cmd.tenantId())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow definition tenant sync request");
        }
        FlowDefinition source = flowDefinitionGateway
                .findById(cmd.id())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "Flow definition not found: " + cmd.id()));
        flowDefinitionGateway
                .findByFlowCodeAndTenantId(source.getFlowCode(), cmd.tenantId())
                .ifPresent(existing -> {
                    throw new BizException(
                            CommonErrorCode.BAD_REQUEST,
                            "Flow definition code already exists: " + existing.getFlowCode());
                });
        FlowDefinition synced = new FlowDefinition();
        synced.setFlowCode(source.getFlowCode());
        synced.setFlowName(source.getFlowName());
        synced.setModelType(source.getModelType());
        synced.setCategoryId(source.getCategoryId());
        synced.setVersion(source.getVersion());
        synced.setPublishStatus(CreateFlowDefinitionCmdExe.UNPUBLISHED);
        synced.setCustomForm(source.getCustomForm());
        synced.setFormPath(source.getFormPath());
        synced.setActiveStatus(CreateFlowDefinitionCmdExe.ACTIVE);
        synced.setListener(source.getListener());
        synced.setExtJson(source.getExtJson());
        synced.setTenantId(cmd.tenantId());
        synced.setDefinitionJson(source.getDefinitionJson());
        synced.setDefinitionXml(source.getDefinitionXml());
        return flowDefinitionGateway.save(synced);
    }
}
