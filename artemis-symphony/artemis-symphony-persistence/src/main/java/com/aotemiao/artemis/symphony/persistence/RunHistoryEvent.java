package com.aotemiao.artemis.symphony.persistence;

import java.time.Instant;

public record RunHistoryEvent(
        long id, String runId, Instant eventTime, String eventType, String sessionId, String payload) {}
