package com.aotemiao.artemis.system.app.query.tenant;

import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FindTenantPackageByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantPackageGateway tenantPackageGateway;

    public FindTenantPackageByIdQryExe(TenantPackageGateway tenantPackageGateway) {
        this.tenantPackageGateway = tenantPackageGateway;
    }

    public Optional<TenantPackage> execute(FindTenantPackageByIdQry qry) {
        return tenantPackageGateway.findById(qry.id());
    }
}
