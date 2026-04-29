package com.aotemiao.artemis.resource.client.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "DTO stores an immutable defensive copy of phone numbers for remote serialization.")
public record SmsBatchSendRequest(List<String> phones, String content, String provider, String extJson) {

    public SmsBatchSendRequest {
        phones = phones == null ? List.of() : List.copyOf(phones);
    }
}
