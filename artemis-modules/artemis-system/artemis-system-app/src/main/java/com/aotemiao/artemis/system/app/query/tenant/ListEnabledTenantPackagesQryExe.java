package com.aotemiao.artemis.system.app.query.tenant;

import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListEnabledTenantPackagesQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantPackageGateway tenantPackageGateway;

    public ListEnabledTenantPackagesQryExe(TenantPackageGateway tenantPackageGateway) {
        this.tenantPackageGateway = tenantPackageGateway;
    }

    public List<TenantPackage> execute(ListEnabledTenantPackagesQry qry) {
        return tenantPackageGateway.findEnabled();
    }
}
