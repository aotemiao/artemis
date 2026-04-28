package com.aotemiao.artemis.system.app.query.post;

import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询系统岗位列表执行器。 */
@Component
public class ListSystemPostQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemPostGateway systemPostGateway;

    public ListSystemPostQryExe(SystemPostGateway systemPostGateway) {
        this.systemPostGateway = systemPostGateway;
    }

    public List<SystemPost> execute(ListSystemPostQry qry) {
        return systemPostGateway.findAll();
    }
}
