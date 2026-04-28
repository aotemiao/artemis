package com.aotemiao.artemis.system.app.query.config;

import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询系统参数执行器。 */
@Component
public class FindSystemConfigByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemConfigGateway systemConfigGateway;

    public FindSystemConfigByIdQryExe(SystemConfigGateway systemConfigGateway) {
        this.systemConfigGateway = systemConfigGateway;
    }

    public Optional<SystemConfig> execute(FindSystemConfigByIdQry qry) {
        return systemConfigGateway.findById(qry.id());
    }
}
