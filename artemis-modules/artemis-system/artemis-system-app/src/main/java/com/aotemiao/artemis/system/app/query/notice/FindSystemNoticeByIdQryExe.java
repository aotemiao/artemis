package com.aotemiao.artemis.system.app.query.notice;

import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询系统通知公告执行器。 */
@Component
public class FindSystemNoticeByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemNoticeGateway systemNoticeGateway;

    public FindSystemNoticeByIdQryExe(SystemNoticeGateway systemNoticeGateway) {
        this.systemNoticeGateway = systemNoticeGateway;
    }

    public Optional<SystemNotice> execute(FindSystemNoticeByIdQry qry) {
        return systemNoticeGateway.findById(qry.id());
    }
}
