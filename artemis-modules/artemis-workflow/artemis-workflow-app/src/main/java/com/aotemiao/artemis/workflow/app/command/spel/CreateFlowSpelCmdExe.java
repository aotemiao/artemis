package com.aotemiao.artemis.workflow.app.command.spel;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增流程 SpEL 表达式命令执行器。 */
@Component
public class CreateFlowSpelCmdExe {

    private final FlowSpelGateway flowSpelGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public CreateFlowSpelCmdExe(FlowSpelGateway flowSpelGateway) {
        this.flowSpelGateway = flowSpelGateway;
    }

    public FlowSpel execute(CreateFlowSpelCmd cmd) {
        FlowSpelPayload payload = requirePayload(cmd == null ? null : cmd.payload());
        ensurePreviewExpressionUnique(payload.previewExpression(), null);
        FlowSpel spel = new FlowSpel();
        fill(spel, payload);
        return flowSpelGateway.save(spel);
    }

    static FlowSpelPayload requirePayload(FlowSpelPayload payload) {
        if (payload == null
                || isBlank(payload.componentName())
                || isBlank(payload.methodName())
                || isBlank(payload.previewExpression())) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST,
                    "Flow SpEL component, method and preview expression must not be blank");
        }
        return payload;
    }

    void ensurePreviewExpressionUnique(String previewExpression, Long currentId) {
        flowSpelGateway.findByPreviewExpression(previewExpression).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST,
                        "Flow SpEL preview expression already exists: " + previewExpression);
            }
        });
    }

    static void fill(FlowSpel spel, FlowSpelPayload payload) {
        Integer status = payload.status();
        spel.setComponentName(payload.componentName());
        spel.setMethodName(payload.methodName());
        spel.setParameters(payload.parameters());
        spel.setPreviewExpression(payload.previewExpression());
        spel.setRemarks(payload.remarks());
        spel.setStatus(status == null ? Integer.valueOf(1) : status);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
