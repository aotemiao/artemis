package com.aotemiao.artemis.workflow.app.command.spel;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 修改流程 SpEL 表达式命令执行器。 */
@Component
public class UpdateFlowSpelCmdExe {

    private final FlowSpelGateway flowSpelGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public UpdateFlowSpelCmdExe(FlowSpelGateway flowSpelGateway) {
        this.flowSpelGateway = flowSpelGateway;
    }

    public FlowSpel execute(UpdateFlowSpelCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow SpEL id");
        }
        FlowSpelPayload payload = CreateFlowSpelCmdExe.requirePayload(cmd.payload());
        FlowSpel existing = flowSpelGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Flow SpEL not found: " + cmd.id()));
        flowSpelGateway.findByPreviewExpression(payload.previewExpression()).ifPresent(found -> {
            if (!found.getId().equals(existing.getId())) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST,
                        "Flow SpEL preview expression already exists: " + payload.previewExpression());
            }
        });
        CreateFlowSpelCmdExe.fill(existing, payload);
        return flowSpelGateway.save(existing);
    }
}
