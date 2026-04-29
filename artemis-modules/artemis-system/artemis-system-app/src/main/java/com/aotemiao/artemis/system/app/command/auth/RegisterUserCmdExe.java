package com.aotemiao.artemis.system.app.command.auth;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.app.service.tenant.TenantRuntimeService;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.user.SystemUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserCmdExe {

    private static final String REGISTER_SWITCH_KEY = "sys.account.registerUser";

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemUserGateway systemUserGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the cache as a managed collaborator; this executor does not expose it.")
    private final SystemConfigCache systemConfigCache;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the service as a managed collaborator; this executor does not expose it.")
    private final TenantRuntimeService tenantRuntimeService;

    public RegisterUserCmdExe(
            SystemUserGateway systemUserGateway,
            SystemConfigCache systemConfigCache,
            TenantRuntimeService tenantRuntimeService) {
        this.systemUserGateway = systemUserGateway;
        this.systemConfigCache = systemConfigCache;
        this.tenantRuntimeService = tenantRuntimeService;
    }

    public Long execute(RegisterUserCmd cmd) {
        if (!registerEnabled()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "User registration is disabled");
        }
        if (!"SYSTEM".equals(cmd.userType())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Unsupported user type: " + cmd.userType());
        }
        String tenantNo = tenantRuntimeService.normalizeTenantNo(cmd.tenantId());
        tenantRuntimeService.ensureUserCapacity(tenantNo);
        systemUserGateway.findByUsername(cmd.username()).ifPresent(existing -> {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Username already exists: " + cmd.username());
        });

        SystemUser systemUser = new SystemUser();
        systemUser.setTenantNo(tenantNo);
        systemUser.setUsername(cmd.username());
        systemUser.setDisplayName(cmd.username());
        systemUser.setPassword(cmd.password());
        systemUser.setEnabled(true);
        return systemUserGateway.save(systemUser).getId();
    }

    private boolean registerEnabled() {
        return systemConfigCache
                .getValue(REGISTER_SWITCH_KEY)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }
}
