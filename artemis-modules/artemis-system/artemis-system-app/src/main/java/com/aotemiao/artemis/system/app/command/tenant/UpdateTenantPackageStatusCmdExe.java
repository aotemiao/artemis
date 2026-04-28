package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class UpdateTenantPackageStatusCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantPackageGateway tenantPackageGateway;

    public UpdateTenantPackageStatusCmdExe(TenantPackageGateway tenantPackageGateway) {
        this.tenantPackageGateway = tenantPackageGateway;
    }

    public TenantPackage execute(UpdateTenantPackageStatusCmd cmd) {
        TenantPackage tenantPackage = tenantPackageGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "TenantPackage not found: " + cmd.id()));
        tenantPackage.setEnabled(cmd.enabled() == null || cmd.enabled());
        return tenantPackageGateway.save(tenantPackage);
    }
}
