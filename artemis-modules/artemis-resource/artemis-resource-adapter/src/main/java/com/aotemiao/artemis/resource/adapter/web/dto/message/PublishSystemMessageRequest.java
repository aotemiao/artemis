package com.aotemiao.artemis.resource.adapter.web.dto.message;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Request DTO stores an immutable defensive copy of recipient ids.")
public record PublishSystemMessageRequest(
        String title,
        String content,
        String sender,
        Long recipientUserId,
        List<Long> recipientUserIds,
        String extJson) {

    public PublishSystemMessageRequest {
        recipientUserIds = recipientUserIds == null ? List.of() : List.copyOf(recipientUserIds);
    }
}
