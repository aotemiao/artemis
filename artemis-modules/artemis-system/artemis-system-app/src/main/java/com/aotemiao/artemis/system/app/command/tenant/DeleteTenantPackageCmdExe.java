package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class DeleteTenantPackageCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantPackageGateway tenantPackageGateway;

    public DeleteTenantPackageCmdExe(TenantPackageGateway tenantPackageGateway) {
        this.tenantPackageGateway = tenantPackageGateway;
    }

    public void execute(DeleteTenantPackageCmd cmd) {
        tenantPackageGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "TenantPackage not found: " + cmd.id()));
        if (tenantPackageGateway.isUsedByTenant(cmd.id())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Tenant package is used by tenant: " + cmd.id());
        }
        tenantPackageGateway.deleteById(cmd.id());
    }
}
