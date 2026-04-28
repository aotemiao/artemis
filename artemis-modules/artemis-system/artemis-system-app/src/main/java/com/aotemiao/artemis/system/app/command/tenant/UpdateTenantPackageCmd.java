package com.aotemiao.artemis.system.app.command.tenant;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

/** 修改租户套餐命令。 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Command records are request boundary objects; the executor copies menu ids into domain model.")
public record UpdateTenantPackageCmd(
        Long id, String packageName, Boolean menuCheckStrictly, Boolean enabled, String remarks, List<Long> menuIds) {}
