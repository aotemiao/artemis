package com.aotemiao.artemis.resource.app.command.notify;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.util.List;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Command stores an immutable defensive copy of phone numbers.")
public record SendSmsCmd(
        String phone,
        List<String> phones,
        String content,
        String templateCode,
        String templateParams,
        String scene,
        LocalDateTime delayedAt,
        String provider,
        String extJson) {

    public SendSmsCmd {
        phones = phones == null ? List.of() : List.copyOf(phones);
    }
}
