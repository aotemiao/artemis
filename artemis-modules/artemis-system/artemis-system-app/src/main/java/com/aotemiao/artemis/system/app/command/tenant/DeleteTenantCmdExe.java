package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteTenantCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantGateway tenantGateway;

    public DeleteTenantCmdExe(TenantGateway tenantGateway) {
        this.tenantGateway = tenantGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(DeleteTenantCmd cmd) {
        var tenant = tenantGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Tenant not found: " + cmd.id()));
        TenantGuard.ensureMutable(tenant);
        tenantGateway.deleteById(cmd.id());
    }
}
