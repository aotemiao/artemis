package com.aotemiao.artemis.symphony.core.model;

import java.time.Instant;
import java.util.Map;

/**
 * Event emitted from agent runner to orchestrator (SPEC Section 10.4).
 */
public record CodexUpdateEvent(
        String event,
        Instant timestamp,
        String codexAppServerPid,
        Map<String, Object> usage,
        Map<String, Object> payload) {}
