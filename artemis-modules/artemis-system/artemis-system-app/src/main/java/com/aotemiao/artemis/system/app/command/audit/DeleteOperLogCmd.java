package com.aotemiao.artemis.system.app.command.audit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

/** 删除后台操作日志命令。 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Command records are request boundary objects; the executor does not mutate the id list.")
public record DeleteOperLogCmd(List<Long> ids) {}
