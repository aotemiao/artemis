package com.aotemiao.artemis.system.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除系统参数命令执行器。 */
@Component
public class DeleteSystemConfigCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects collaborators as managed beans; this executor does not expose them.")
    private final SystemConfigGateway systemConfigGateway;

    private final SystemConfigCache systemConfigCache;

    public DeleteSystemConfigCmdExe(SystemConfigGateway systemConfigGateway, SystemConfigCache systemConfigCache) {
        this.systemConfigGateway = systemConfigGateway;
        this.systemConfigCache = systemConfigCache;
    }

    public void execute(DeleteSystemConfigCmd cmd) {
        SystemConfig systemConfig = systemConfigGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemConfig not found: " + cmd.id()));
        if (systemConfig.isSystemBuiltIn()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Built-in config cannot be deleted: " + cmd.id());
        }
        systemConfigGateway.deleteById(cmd.id());
        systemConfigCache.evict(systemConfig.getConfigKey());
    }
}
