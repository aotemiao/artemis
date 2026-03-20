package com.aotemiao.artemis.symphony.core.model;

/** 单次运行阶段 / 终止原因。见 SPEC 第 7.2 节。 */
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
