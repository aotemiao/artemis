package com.aotemiao.artemis.system.app.query.audit;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class OperLogPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final OperLogGateway operLogGateway;

    public OperLogPageQryExe(OperLogGateway operLogGateway) {
        this.operLogGateway = operLogGateway;
    }

    public PageResult<OperLog> execute(OperLogPageQry qry) {
        return operLogGateway.findPage(qry.pageRequest());
    }
}
