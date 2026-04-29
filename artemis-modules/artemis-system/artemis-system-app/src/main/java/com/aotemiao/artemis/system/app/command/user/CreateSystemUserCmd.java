package com.aotemiao.artemis.system.app.command.user;

/** 新增系统用户命令。 */
public record CreateSystemUserCmd(String tenantNo, String username, String displayName, String password) {

    public CreateSystemUserCmd(String username, String displayName, String password) {
        this("000000", username, displayName, password);
    }
}
