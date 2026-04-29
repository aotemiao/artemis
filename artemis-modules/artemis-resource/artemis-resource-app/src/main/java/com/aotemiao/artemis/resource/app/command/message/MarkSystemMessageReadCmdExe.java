package com.aotemiao.artemis.resource.app.command.message;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.message.SystemMessageGateway;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class MarkSystemMessageReadCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final SystemMessageGateway systemMessageGateway;

    public MarkSystemMessageReadCmdExe(SystemMessageGateway systemMessageGateway) {
        this.systemMessageGateway = systemMessageGateway;
    }

    public SystemMessage execute(MarkSystemMessageReadCmd cmd) {
        SystemMessage message = systemMessageGateway
                .findById(cmd.id())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "System message not found: " + cmd.id()));
        if (!message.getRecipientUserId().equals(cmd.recipientUserId())) {
            throw new BizException(CommonErrorCode.FORBIDDEN, "System message does not belong to user");
        }
        if (!message.isRead()) {
            message.setReadFlag(1);
            message.setReadTime(LocalDateTime.now());
            return systemMessageGateway.save(message);
        }
        return message;
    }
}
