package com.aotemiao.artemis.resource.client.dto.message;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "DTO stores an immutable defensive copy of recipient ids for remote serialization.")
public record PublishMessageRequest(
        String title,
        String content,
        String sender,
        Long recipientUserId,
        List<Long> recipientUserIds,
        String extJson) {

    public PublishMessageRequest {
        recipientUserIds = recipientUserIds == null ? List.of() : List.copyOf(recipientUserIds);
    }
}
