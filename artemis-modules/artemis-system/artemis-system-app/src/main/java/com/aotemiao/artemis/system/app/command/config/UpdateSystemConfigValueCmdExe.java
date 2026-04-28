package com.aotemiao.artemis.system.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 按参数 key 更新参数值命令执行器。 */
@Component
public class UpdateSystemConfigValueCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects collaborators as managed beans; this executor does not expose them.")
    private final SystemConfigGateway systemConfigGateway;

    private final SystemConfigCache systemConfigCache;

    public UpdateSystemConfigValueCmdExe(SystemConfigGateway systemConfigGateway, SystemConfigCache systemConfigCache) {
        this.systemConfigGateway = systemConfigGateway;
        this.systemConfigCache = systemConfigCache;
    }

    public SystemConfig execute(UpdateSystemConfigValueCmd cmd) {
        SystemConfig systemConfig = systemConfigGateway
                .findByConfigKey(cmd.configKey())
                .orElseThrow(() ->
                        new BizException(CommonErrorCode.NOT_FOUND, "SystemConfig not found: " + cmd.configKey()));
        systemConfig.setConfigValue(cmd.configValue());
        SystemConfig saved = systemConfigGateway.save(systemConfig);
        systemConfigCache.evict(saved.getConfigKey());
        return saved;
    }
}
