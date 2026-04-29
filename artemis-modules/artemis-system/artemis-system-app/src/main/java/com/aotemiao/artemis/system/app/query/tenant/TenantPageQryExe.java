package com.aotemiao.artemis.system.app.query.tenant;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class TenantPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantGateway tenantGateway;

    public TenantPageQryExe(TenantGateway tenantGateway) {
        this.tenantGateway = tenantGateway;
    }

    public PageResult<Tenant> execute(TenantPageQry qry) {
        return tenantGateway.findPage(qry.pageRequest());
    }
}
