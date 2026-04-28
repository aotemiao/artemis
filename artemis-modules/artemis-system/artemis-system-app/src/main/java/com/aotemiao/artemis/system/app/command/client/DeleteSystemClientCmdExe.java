package com.aotemiao.artemis.system.app.command.client;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除系统客户端命令执行器。 */
@Component
public class DeleteSystemClientCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemClientGateway systemClientGateway;

    public DeleteSystemClientCmdExe(SystemClientGateway systemClientGateway) {
        this.systemClientGateway = systemClientGateway;
    }

    public void execute(DeleteSystemClientCmd cmd) {
        systemClientGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Client not found: " + cmd.id()));
        systemClientGateway.deleteById(cmd.id());
    }
}
