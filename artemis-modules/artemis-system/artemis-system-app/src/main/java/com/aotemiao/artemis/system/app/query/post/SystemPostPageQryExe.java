package com.aotemiao.artemis.system.app.query.post;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 分页查询系统岗位执行器。 */
@Component
public class SystemPostPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemPostGateway systemPostGateway;

    public SystemPostPageQryExe(SystemPostGateway systemPostGateway) {
        this.systemPostGateway = systemPostGateway;
    }

    public PageResult<SystemPost> execute(SystemPostPageQry qry) {
        return systemPostGateway.findPage(qry.pageRequest());
    }
}
