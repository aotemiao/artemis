package com.aotemiao.artemis.symphony.core.model;

/**
 * Run attempt phase / terminal reason per SPEC Section 7.2.
 */
public enum RunAttemptStatus {
    PreparingWorkspace,
    BuildingPrompt,
    LaunchingAgentProcess,
    InitializingSession,
    StreamingTurn,
    Finishing,
    Succeeded,
    Failed,
    TimedOut,
    Stalled,
    CanceledByReconciliation
}
