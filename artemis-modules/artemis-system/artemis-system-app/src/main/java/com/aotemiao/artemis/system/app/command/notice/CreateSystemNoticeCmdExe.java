package com.aotemiao.artemis.system.app.command.notice;

import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增系统通知公告命令执行器。 */
@Component
public class CreateSystemNoticeCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemNoticeGateway systemNoticeGateway;

    public CreateSystemNoticeCmdExe(SystemNoticeGateway systemNoticeGateway) {
        this.systemNoticeGateway = systemNoticeGateway;
    }

    public SystemNotice execute(CreateSystemNoticeCmd cmd) {
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setNoticeTitle(cmd.noticeTitle());
        systemNotice.setNoticeType(cmd.noticeType());
        systemNotice.setNoticeContent(cmd.noticeContent());
        systemNotice.setStatus(cmd.status());
        systemNotice.setRemarks(cmd.remarks());
        return systemNoticeGateway.save(systemNotice);
    }
}
