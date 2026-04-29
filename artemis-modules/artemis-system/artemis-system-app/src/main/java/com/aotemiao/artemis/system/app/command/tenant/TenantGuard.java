package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;

/** 租户写操作保护。 */
final class TenantGuard {

    static final String DEFAULT_TENANT_NO = "000000";

    private TenantGuard() {}

    static void ensureMutable(Tenant tenant) {
        if (tenant == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "Tenant not found");
        }
        if (DEFAULT_TENANT_NO.equals(tenant.getTenantNo())) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST, "Default tenant cannot be operated: " + tenant.getTenantNo());
        }
    }
}
