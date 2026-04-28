package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class CreateTenantPackageCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    private final TenantPackageGateway tenantPackageGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    private final SystemMenuGateway systemMenuGateway;

    public CreateTenantPackageCmdExe(TenantPackageGateway tenantPackageGateway, SystemMenuGateway systemMenuGateway) {
        this.tenantPackageGateway = tenantPackageGateway;
        this.systemMenuGateway = systemMenuGateway;
    }

    public TenantPackage execute(TenantPackageCmd cmd) {
        validateName(cmd.packageName(), null);
        TenantPackage tenantPackage = new TenantPackage();
        fill(tenantPackage, cmd);
        return tenantPackageGateway.save(tenantPackage);
    }

    protected void fill(TenantPackage tenantPackage, TenantPackageCmd cmd) {
        tenantPackage.setPackageName(cmd.packageName());
        tenantPackage.setMenuCheckStrictly(cmd.menuCheckStrictly() == null || cmd.menuCheckStrictly());
        tenantPackage.setEnabled(cmd.enabled() == null || cmd.enabled());
        tenantPackage.setRemarks(cmd.remarks());
        tenantPackage.setMenuIds(validMenuIds(cmd.menuIds()));
    }

    protected void validateName(String packageName, Long excludeId) {
        if (packageName == null || packageName.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Tenant package name must not be blank");
        }
        if (tenantPackageGateway.existsByPackageName(packageName, excludeId)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Tenant package name already exists: " + packageName);
        }
    }

    protected List<Long> validMenuIds(List<Long> rawMenuIds) {
        List<Long> menuIds = rawMenuIds == null
                ? List.of()
                : rawMenuIds.stream().filter(Objects::nonNull).distinct().toList();
        if (!menuIds.isEmpty() && systemMenuGateway.findByIds(menuIds).size() != menuIds.size()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Some menuIds do not exist: " + menuIds);
        }
        return menuIds;
    }
}
