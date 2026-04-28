package com.aotemiao.artemis.system.app.query.tenant;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class TenantPackagePageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantPackageGateway tenantPackageGateway;

    public TenantPackagePageQryExe(TenantPackageGateway tenantPackageGateway) {
        this.tenantPackageGateway = tenantPackageGateway;
    }

    public PageResult<TenantPackage> execute(TenantPackagePageQry qry) {
        return tenantPackageGateway.findPage(qry.pageRequest());
    }
}
