package com.aotemiao.artemis.system.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 更新系统参数命令执行器。 */
@Component
public class UpdateSystemConfigCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects collaborators as managed beans; this executor does not expose them.")
    private final SystemConfigGateway systemConfigGateway;

    private final SystemConfigCache systemConfigCache;

    public UpdateSystemConfigCmdExe(SystemConfigGateway systemConfigGateway, SystemConfigCache systemConfigCache) {
        this.systemConfigGateway = systemConfigGateway;
        this.systemConfigCache = systemConfigCache;
    }

    public SystemConfig execute(UpdateSystemConfigCmd cmd) {
        SystemConfig systemConfig = systemConfigGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemConfig not found: " + cmd.id()));
        String oldConfigKey = systemConfig.getConfigKey();

        systemConfigGateway.findByConfigKey(cmd.configKey()).ifPresent(existing -> {
            if (!existing.getId().equals(cmd.id())) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Config key already exists: " + cmd.configKey());
            }
        });

        systemConfig.setConfigName(cmd.configName());
        systemConfig.setConfigKey(cmd.configKey());
        systemConfig.setConfigValue(cmd.configValue());
        systemConfig.setSystemBuiltIn(Boolean.TRUE.equals(cmd.systemBuiltIn()));
        systemConfig.setRemarks(cmd.remarks());

        SystemConfig saved = systemConfigGateway.save(systemConfig);
        systemConfigCache.evict(oldConfigKey);
        systemConfigCache.evict(saved.getConfigKey());
        return saved;
    }
}
