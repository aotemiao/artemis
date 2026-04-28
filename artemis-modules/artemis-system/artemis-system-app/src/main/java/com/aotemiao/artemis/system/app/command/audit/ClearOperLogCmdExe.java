package com.aotemiao.artemis.system.app.command.audit;

import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class ClearOperLogCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final OperLogGateway operLogGateway;

    public ClearOperLogCmdExe(OperLogGateway operLogGateway) {
        this.operLogGateway = operLogGateway;
    }

    public void execute(ClearOperLogCmd cmd) {
        operLogGateway.clear();
    }
}
