package com.aotemiao.artemis.system.app.query.notice;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 分页查询系统通知公告执行器。 */
@Component
public class SystemNoticePageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemNoticeGateway systemNoticeGateway;

    public SystemNoticePageQryExe(SystemNoticeGateway systemNoticeGateway) {
        this.systemNoticeGateway = systemNoticeGateway;
    }

    public PageResult<SystemNotice> execute(SystemNoticePageQry qry) {
        return systemNoticeGateway.findPage(qry.pageRequest());
    }
}
