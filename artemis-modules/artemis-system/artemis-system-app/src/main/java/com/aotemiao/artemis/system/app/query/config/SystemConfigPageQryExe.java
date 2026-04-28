package com.aotemiao.artemis.system.app.query.config;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 分页查询系统参数执行器。 */
@Component
public class SystemConfigPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemConfigGateway systemConfigGateway;

    public SystemConfigPageQryExe(SystemConfigGateway systemConfigGateway) {
        this.systemConfigGateway = systemConfigGateway;
    }

    public PageResult<SystemConfig> execute(SystemConfigPageQry qry) {
        return systemConfigGateway.findPage(qry.pageRequest());
    }
}
