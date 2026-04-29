package com.aotemiao.artemis.system.app.command.user;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.app.service.tenant.TenantRuntimeService;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.user.SystemUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增系统用户命令执行器。 */
@Component
public class CreateSystemUserCmdExe {

    private static final String INIT_PASSWORD_KEY = "sys.user.initPassword";

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

    public CreateSystemUserCmdExe(
            SystemUserGateway systemUserGateway,
            SystemConfigCache systemConfigCache,
            TenantRuntimeService tenantRuntimeService) {
        this.systemUserGateway = systemUserGateway;
        this.systemConfigCache = systemConfigCache;
        this.tenantRuntimeService = tenantRuntimeService;
    }

    public SystemUser execute(CreateSystemUserCmd cmd) {
        String tenantNo = tenantRuntimeService.normalizeTenantNo(cmd.tenantNo());
        tenantRuntimeService.ensureUserCapacity(tenantNo);
        systemUserGateway.findByUsername(cmd.username()).ifPresent(existing -> {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Username already exists: " + cmd.username());
        });

        SystemUser systemUser = new SystemUser();
        systemUser.setTenantNo(tenantNo);
        systemUser.setUsername(cmd.username());
        systemUser.setDisplayName(cmd.displayName());
        systemUser.setPassword(resolvePassword(cmd.password()));
        systemUser.setEnabled(true);
        return systemUserGateway.save(systemUser);
    }

    private String resolvePassword(String requestedPassword) {
        if (requestedPassword != null && !requestedPassword.isBlank()) {
            return requestedPassword;
        }
        return systemConfigCache.getValue(INIT_PASSWORD_KEY).orElse("123456");
    }
}
