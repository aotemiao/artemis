package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class LookupTypePageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LookupTypeGateway lookupTypeGateway;

    public LookupTypePageQryExe(LookupTypeGateway lookupTypeGateway) {
        this.lookupTypeGateway = lookupTypeGateway;
    }

    public PageResult<LookupType> execute(LookupTypePageQry qry) {
        return lookupTypeGateway.findPage(qry.pageRequest());
    }
}
