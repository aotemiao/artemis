package com.aotemiao.artemis.system.app.service.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/** 租户运行时约束与配额校验。 */
@Component
public class TenantRuntimeService {

    public static final String DEFAULT_TENANT_NO = "000000";
    private static final String NORMAL_STATUS = "NORMAL";

    private final TenantGateway tenantGateway;
    private final SystemUserGateway systemUserGateway;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring 注入领域网关作为受管协作者，服务不会向外暴露该引用。")
    public TenantRuntimeService(TenantGateway tenantGateway, SystemUserGateway systemUserGateway) {
        this.tenantGateway = tenantGateway;
        this.systemUserGateway = systemUserGateway;
    }

    public String normalizeTenantNo(String tenantNo) {
        return tenantNo == null || tenantNo.isBlank() ? DEFAULT_TENANT_NO : tenantNo;
    }

    public boolean canLogin(String tenantNo) {
        String normalizedTenantNo = normalizeTenantNo(tenantNo);
        if (DEFAULT_TENANT_NO.equals(normalizedTenantNo)) {
            return true;
        }
        return tenantGateway
                .findByTenantNo(normalizedTenantNo)
                .filter(this::isActiveTenant)
                .isPresent();
    }

    public void ensureUserCapacity(String tenantNo) {
        String normalizedTenantNo = normalizeTenantNo(tenantNo);
        if (DEFAULT_TENANT_NO.equals(normalizedTenantNo)) {
            return;
        }
        Tenant tenant = tenantGateway
                .findByTenantNo(normalizedTenantNo)
                .filter(this::isActiveTenant)
                .orElseThrow(() ->
                        new BizException(CommonErrorCode.BAD_REQUEST, "Tenant not available: " + normalizedTenantNo));
        Integer userLimit = tenant.getUserLimit();
        if (userLimit == null || userLimit < 0) {
            return;
        }
        long currentUserCount = systemUserGateway.countByTenantNo(normalizedTenantNo);
        if (currentUserCount >= userLimit) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST,
                    "Tenant user limit exceeded: " + normalizedTenantNo + " / " + userLimit);
        }
    }

    private boolean isActiveTenant(Tenant tenant) {
        return NORMAL_STATUS.equals(tenant.getStatus())
                && (tenant.getExpireTime() == null || tenant.getExpireTime().isAfter(LocalDateTime.now()));
    }
}
