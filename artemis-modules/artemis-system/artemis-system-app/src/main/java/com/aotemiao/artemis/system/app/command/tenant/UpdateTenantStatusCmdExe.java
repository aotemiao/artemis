package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateTenantStatusCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantGateway tenantGateway;

    public UpdateTenantStatusCmdExe(TenantGateway tenantGateway) {
        this.tenantGateway = tenantGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public Tenant execute(UpdateTenantStatusCmd cmd) {
        Tenant tenant = tenantGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Tenant not found: " + cmd.id()));
        TenantGuard.ensureMutable(tenant);
        tenant.setStatus(cmd.status());
        return tenantGateway.save(tenant);
    }
}
