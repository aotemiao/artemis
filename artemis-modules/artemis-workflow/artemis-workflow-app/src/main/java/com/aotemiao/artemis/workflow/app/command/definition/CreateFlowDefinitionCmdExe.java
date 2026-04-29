package com.aotemiao.artemis.workflow.app.command.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.definition.FlowDefinitionGateway;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增流程定义命令执行器。 */
@Component
public class CreateFlowDefinitionCmdExe {

    static final int UNPUBLISHED = 0;
    static final int PUBLISHED = 1;
    static final int ACTIVE = 1;
    static final int SUSPENDED = 0;

    private final FlowDefinitionGateway flowDefinitionGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public CreateFlowDefinitionCmdExe(FlowDefinitionGateway flowDefinitionGateway) {
        this.flowDefinitionGateway = flowDefinitionGateway;
    }

    public FlowDefinition execute(CreateFlowDefinitionCmd cmd) {
        FlowDefinitionPayload payload = requirePayload(cmd == null ? null : cmd.payload());
        ensureFlowCodeUnique(payload.flowCode(), payload.tenantId(), null);
        FlowDefinition definition = new FlowDefinition();
        fill(definition, payload);
        definition.setPublishStatus(UNPUBLISHED);
        definition.setActiveStatus(ACTIVE);
        return flowDefinitionGateway.save(definition);
    }

    static FlowDefinitionPayload requirePayload(FlowDefinitionPayload payload) {
        if (payload == null
                || isBlank(payload.flowCode())
                || isBlank(payload.flowName())
                || isBlank(payload.modelType())
                || isBlank(payload.tenantId())) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST,
                    "Flow definition code, name, model type and tenant id must not be blank");
        }
        return payload;
    }

    void ensureFlowCodeUnique(String flowCode, String tenantId, Long currentId) {
        flowDefinitionGateway.findByFlowCodeAndTenantId(flowCode, tenantId).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow definition code already exists: " + flowCode);
            }
        });
    }

    static void fill(FlowDefinition definition, FlowDefinitionPayload payload) {
        definition.setFlowCode(payload.flowCode());
        definition.setFlowName(payload.flowName());
        definition.setModelType(payload.modelType());
        definition.setCategoryId(payload.categoryId());
        definition.setVersion(payload.version() == null ? Integer.valueOf(1) : payload.version());
        definition.setCustomForm(Boolean.TRUE.equals(payload.customForm()));
        definition.setFormPath(payload.formPath());
        definition.setListener(payload.listener());
        definition.setExtJson(payload.extJson());
        definition.setTenantId(payload.tenantId());
        definition.setDefinitionJson(payload.definitionJson());
        definition.setDefinitionXml(payload.definitionXml());
    }

    static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
