package com.aotemiao.artemis.system.app.command.audit;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class DeleteOperLogCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final OperLogGateway operLogGateway;

    public DeleteOperLogCmdExe(OperLogGateway operLogGateway) {
        this.operLogGateway = operLogGateway;
    }

    public void execute(DeleteOperLogCmd cmd) {
        if (cmd.ids() == null || cmd.ids().isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "OperLog ids must not be empty");
        }
        operLogGateway.deleteByIds(cmd.ids());
    }
}
