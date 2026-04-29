package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateTenantCmdExe extends CreateTenantCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final TenantGateway tenantGateway;

    public UpdateTenantCmdExe(
            TenantGateway tenantGateway,
            com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway tenantPackageGateway,
            TenantBootstrapService tenantBootstrapService) {
        super(tenantGateway, tenantPackageGateway, tenantBootstrapService);
        this.tenantGateway = tenantGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public Tenant execute(UpdateTenantCmd cmd) {
        Tenant tenant = tenantGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Tenant not found: " + cmd.id()));
        TenantGuard.ensureMutable(tenant);
        validateCompanyName(cmd.companyName(), cmd.id());
        tenant.setCompanyName(cmd.companyName());
        tenant.setContactName(cmd.contactName());
        tenant.setContactPhone(cmd.contactPhone());
        tenant.setSocialCreditCode(cmd.socialCreditCode());
        tenant.setAddress(cmd.address());
        tenant.setDomain(cmd.domain());
        tenant.setIntro(cmd.intro());
        tenant.setExpireTime(cmd.expireTime());
        tenant.setUserLimit(cmd.userLimit());
        tenant.setRemarks(cmd.remarks());
        return tenantGateway.save(tenant);
    }
}
