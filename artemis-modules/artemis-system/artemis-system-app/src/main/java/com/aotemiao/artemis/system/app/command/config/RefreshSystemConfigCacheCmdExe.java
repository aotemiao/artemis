package com.aotemiao.artemis.system.app.command.config;

import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 刷新系统参数缓存命令执行器。 */
@Component
public class RefreshSystemConfigCacheCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the cache as a managed collaborator; this executor does not expose it.")
    private final SystemConfigCache systemConfigCache;

    public RefreshSystemConfigCacheCmdExe(SystemConfigCache systemConfigCache) {
        this.systemConfigCache = systemConfigCache;
    }

    public void execute(RefreshSystemConfigCacheCmd cmd) {
        systemConfigCache.refresh();
    }
}
