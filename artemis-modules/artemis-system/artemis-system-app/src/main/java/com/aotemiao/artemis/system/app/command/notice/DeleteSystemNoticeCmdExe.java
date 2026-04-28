package com.aotemiao.artemis.system.app.command.notice;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除系统通知公告命令执行器。 */
@Component
public class DeleteSystemNoticeCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemNoticeGateway systemNoticeGateway;

    public DeleteSystemNoticeCmdExe(SystemNoticeGateway systemNoticeGateway) {
        this.systemNoticeGateway = systemNoticeGateway;
    }

    public void execute(DeleteSystemNoticeCmd cmd) {
        systemNoticeGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemNotice not found: " + cmd.id()));
        systemNoticeGateway.deleteById(cmd.id());
    }
}
