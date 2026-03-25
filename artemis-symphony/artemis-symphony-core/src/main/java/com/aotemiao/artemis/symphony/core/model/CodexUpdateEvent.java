package com.aotemiao.artemis.symphony.core.model;

import java.time.Instant;
import java.util.Map;

/** 由 Agent 运行器向编排器上报的事件。见 SPEC 第 10.4 节。 */
public record CodexUpdateEvent(
        String event,
        Instant timestamp,
        String codexAppServerPid,
        Map<String, Object> usage,
        Map<String, Object> payload) {

    public CodexUpdateEvent {
        if (usage == null && payload != null) {
            Object usageFromPayload = payload.get("usage");
            if (usageFromPayload instanceof Map<?, ?> usageMap) {
                usage = usageMap.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                e -> String.valueOf(e.getKey()), Map.Entry::getValue));
            }
        }
        usage = usage != null ? Map.copyOf(usage) : Map.of();
        payload = payload != null ? Map.copyOf(payload) : Map.of();
    }

    @Override
    public Map<String, Object> usage() {
        return Map.copyOf(usage);
    }

    @Override
    public Map<String, Object> payload() {
        return Map.copyOf(payload);
    }
}
