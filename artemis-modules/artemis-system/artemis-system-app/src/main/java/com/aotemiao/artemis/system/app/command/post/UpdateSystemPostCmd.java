package com.aotemiao.artemis.system.app.command.post;

/** 更新系统岗位命令。 */
public record UpdateSystemPostCmd(
        Long id,
        Long deptId,
        String postCode,
        String postCategory,
        String postName,
        Integer sortOrder,
        String status,
        String remarks) {}
