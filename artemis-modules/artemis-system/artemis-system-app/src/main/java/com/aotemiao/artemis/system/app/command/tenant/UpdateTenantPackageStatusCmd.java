package com.aotemiao.artemis.system.app.command.tenant;

/** 修改租户套餐状态命令。 */
public record UpdateTenantPackageStatusCmd(Long id, Boolean enabled) {}
