package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class UpdateTenantPackageCmdExe extends CreateTenantPackageCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantPackageGateway tenantPackageGateway;

    public UpdateTenantPackageCmdExe(TenantPackageGateway tenantPackageGateway, SystemMenuGateway systemMenuGateway) {
        super(tenantPackageGateway, systemMenuGateway);
        this.tenantPackageGateway = tenantPackageGateway;
    }

    public TenantPackage execute(UpdateTenantPackageCmd cmd) {
        TenantPackage tenantPackage = tenantPackageGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "TenantPackage not found: " + cmd.id()));
        validateName(cmd.packageName(), cmd.id());
        fill(
                tenantPackage,
                new TenantPackageCmd(
                        cmd.packageName(), cmd.menuCheckStrictly(), cmd.enabled(), cmd.remarks(), cmd.menuIds()));
        return tenantPackageGateway.save(tenantPackage);
    }
}
