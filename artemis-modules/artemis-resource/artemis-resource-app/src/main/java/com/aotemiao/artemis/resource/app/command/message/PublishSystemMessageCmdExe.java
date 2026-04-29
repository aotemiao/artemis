package com.aotemiao.artemis.resource.app.command.message;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.message.SystemMessageGateway;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishSystemMessageCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final SystemMessageGateway systemMessageGateway;

    public PublishSystemMessageCmdExe(SystemMessageGateway systemMessageGateway) {
        this.systemMessageGateway = systemMessageGateway;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SystemMessage> execute(PublishSystemMessageCmd cmd) {
        requireText(cmd.title(), "Message title must not be blank");
        requireText(cmd.content(), "Message content must not be blank");
        List<Long> recipients = recipients(cmd);
        List<SystemMessage> saved = new ArrayList<>();
        boolean broadcast = recipients.size() > 1;
        for (Long recipient : recipients) {
            saved.add(systemMessageGateway.save(newMessage(cmd, recipient, broadcast)));
        }
        return saved;
    }

    private List<Long> recipients(PublishSystemMessageCmd cmd) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (cmd.recipientUserId() != null) {
            ids.add(cmd.recipientUserId());
        }
        if (cmd.recipientUserIds() != null) {
            ids.addAll(cmd.recipientUserIds().stream()
                    .filter(id -> id != null && id > 0)
                    .toList());
        }
        if (ids.isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Recipient user must not be empty");
        }
        return ids.stream().toList();
    }

    private SystemMessage newMessage(PublishSystemMessageCmd cmd, Long recipientUserId, boolean broadcast) {
        SystemMessage message = new SystemMessage();
        message.setTitle(cmd.title().strip());
        message.setContent(cmd.content().strip());
        message.setSender(
                cmd.sender() == null || cmd.sender().isBlank()
                        ? "system"
                        : cmd.sender().strip());
        message.setRecipientUserId(recipientUserId);
        message.setBroadcastFlag(broadcast ? Integer.valueOf(1) : Integer.valueOf(0));
        message.setReadFlag(0);
        message.setExtJson(cmd.extJson());
        return message;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, message);
        }
    }
}
