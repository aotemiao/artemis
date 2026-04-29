package com.aotemiao.artemis.workflow.app.command.spel;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.spel.FlowSpelGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除流程 SpEL 表达式命令执行器。 */
@Component
public class DeleteFlowSpelCmdExe {

    private final FlowSpelGateway flowSpelGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public DeleteFlowSpelCmdExe(FlowSpelGateway flowSpelGateway) {
        this.flowSpelGateway = flowSpelGateway;
    }

    public void execute(DeleteFlowSpelCmd cmd) {
        Long id = cmd == null ? null : cmd.id();
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow SpEL id: " + id);
        }
        flowSpelGateway
                .findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Flow SpEL not found: " + id));
        flowSpelGateway.deleteById(id);
    }
}
