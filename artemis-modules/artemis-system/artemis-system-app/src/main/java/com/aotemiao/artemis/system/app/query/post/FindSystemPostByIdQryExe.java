package com.aotemiao.artemis.system.app.query.post;

import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询系统岗位执行器。 */
@Component
public class FindSystemPostByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemPostGateway systemPostGateway;

    public FindSystemPostByIdQryExe(SystemPostGateway systemPostGateway) {
        this.systemPostGateway = systemPostGateway;
    }

    public Optional<SystemPost> execute(FindSystemPostByIdQry qry) {
        return systemPostGateway.findById(qry.id());
    }
}
