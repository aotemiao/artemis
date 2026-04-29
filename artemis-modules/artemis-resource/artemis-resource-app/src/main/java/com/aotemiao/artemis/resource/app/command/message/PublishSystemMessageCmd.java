package com.aotemiao.artemis.resource.app.command.message;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Command stores an immutable defensive copy of recipient ids.")
public record PublishSystemMessageCmd(
        String title,
        String content,
        String sender,
        Long recipientUserId,
        List<Long> recipientUserIds,
        String extJson) {

    public PublishSystemMessageCmd {
        recipientUserIds = recipientUserIds == null ? List.of() : List.copyOf(recipientUserIds);
    }
}
