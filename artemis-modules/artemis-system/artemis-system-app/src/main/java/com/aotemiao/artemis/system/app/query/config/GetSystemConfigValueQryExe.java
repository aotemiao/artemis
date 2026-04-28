package com.aotemiao.artemis.system.app.query.config;

import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 key 查询系统参数值执行器。 */
@Component
public class GetSystemConfigValueQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the cache as a managed collaborator; this query executor does not expose it.")
    private final SystemConfigCache systemConfigCache;

    public GetSystemConfigValueQryExe(SystemConfigCache systemConfigCache) {
        this.systemConfigCache = systemConfigCache;
    }

    public Optional<String> execute(GetSystemConfigValueQry qry) {
        return systemConfigCache.getValue(qry.configKey());
    }
}
