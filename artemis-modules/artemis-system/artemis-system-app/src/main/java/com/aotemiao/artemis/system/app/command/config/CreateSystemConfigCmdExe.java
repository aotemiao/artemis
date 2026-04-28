package com.aotemiao.artemis.system.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增系统参数命令执行器。 */
@Component
public class CreateSystemConfigCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects collaborators as managed beans; this executor does not expose them.")
    private final SystemConfigGateway systemConfigGateway;

    private final SystemConfigCache systemConfigCache;

    public CreateSystemConfigCmdExe(SystemConfigGateway systemConfigGateway, SystemConfigCache systemConfigCache) {
        this.systemConfigGateway = systemConfigGateway;
        this.systemConfigCache = systemConfigCache;
    }

    public SystemConfig execute(CreateSystemConfigCmd cmd) {
        systemConfigGateway.findByConfigKey(cmd.configKey()).ifPresent(existing -> {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Config key already exists: " + cmd.configKey());
        });

        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setConfigName(cmd.configName());
        systemConfig.setConfigKey(cmd.configKey());
        systemConfig.setConfigValue(cmd.configValue());
        systemConfig.setSystemBuiltIn(Boolean.TRUE.equals(cmd.systemBuiltIn()));
        systemConfig.setRemarks(cmd.remarks());

        SystemConfig saved = systemConfigGateway.save(systemConfig);
        systemConfigCache.evict(saved.getConfigKey());
        return saved;
    }
}
