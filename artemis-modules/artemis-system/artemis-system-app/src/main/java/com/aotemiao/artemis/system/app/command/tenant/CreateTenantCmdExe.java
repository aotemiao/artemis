package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateTenantCmdExe {

    private static final SecureRandom RANDOM = new SecureRandom();

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateways and collaborator as managed beans; this executor does not expose them.")
    private final TenantGateway tenantGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateways and collaborator as managed beans; this executor does not expose them.")
    private final TenantPackageGateway tenantPackageGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the collaborator as a managed bean; this executor does not expose it.")
    private final TenantBootstrapService tenantBootstrapService;

    public CreateTenantCmdExe(
            TenantGateway tenantGateway,
            TenantPackageGateway tenantPackageGateway,
            TenantBootstrapService tenantBootstrapService) {
        this.tenantGateway = tenantGateway;
        this.tenantPackageGateway = tenantPackageGateway;
        this.tenantBootstrapService = tenantBootstrapService;
    }

    @Transactional(rollbackFor = Exception.class)
    public Tenant execute(CreateTenantCmd cmd) {
        validateCompanyName(cmd.companyName(), null);
        validatePackageId(cmd.packageId());
        Tenant tenant = new Tenant();
        fill(
                tenant,
                new TenantCmd(
                        cmd.companyName(),
                        cmd.contactName(),
                        cmd.contactPhone(),
                        cmd.socialCreditCode(),
                        cmd.address(),
                        cmd.domain(),
                        cmd.intro(),
                        cmd.packageId(),
                        cmd.expireTime(),
                        cmd.userLimit(),
                        "NORMAL",
                        cmd.remarks()));
        tenant.setTenantNo(generateTenantNo());
        tenant = tenantGateway.save(tenant);
        tenantBootstrapService.bootstrap(tenant);
        return tenant;
    }

    protected void fill(Tenant tenant, TenantCmd cmd) {
        tenant.setCompanyName(cmd.companyName());
        tenant.setContactName(cmd.contactName());
        tenant.setContactPhone(cmd.contactPhone());
        tenant.setSocialCreditCode(cmd.socialCreditCode());
        tenant.setAddress(cmd.address());
        tenant.setDomain(cmd.domain());
        tenant.setIntro(cmd.intro());
        tenant.setPackageId(cmd.packageId());
        tenant.setExpireTime(cmd.expireTime());
        tenant.setUserLimit(cmd.userLimit());
        tenant.setStatus(cmd.status() == null || cmd.status().isBlank() ? "NORMAL" : cmd.status());
        tenant.setRemarks(cmd.remarks());
    }

    protected void validateCompanyName(String companyName, Long excludeId) {
        if (companyName == null || companyName.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Tenant company name must not be blank");
        }
        if (tenantGateway.existsByCompanyName(companyName, excludeId)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Tenant company name already exists: " + companyName);
        }
    }

    protected void validatePackageId(Long packageId) {
        if (packageId == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Tenant packageId must not be blank");
        }
        if (tenantPackageGateway.findById(packageId).isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Tenant package not found: " + packageId);
        }
    }

    protected String generateTenantNo() {
        for (int i = 0; i < 20; i++) {
            String tenantNo = String.valueOf(RANDOM.nextInt(100000, 1_000_000));
            if (!tenantGateway.existsByTenantNo(tenantNo, null)) {
                return tenantNo;
            }
        }
        throw new BizException(CommonErrorCode.INTERNAL_ERROR, "Failed to generate unique tenantNo");
    }
}
