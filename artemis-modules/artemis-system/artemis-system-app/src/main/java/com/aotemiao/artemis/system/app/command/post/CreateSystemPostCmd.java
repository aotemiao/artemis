package com.aotemiao.artemis.system.app.command.post;

/** 创建系统岗位命令。 */
public record CreateSystemPostCmd(
        Long deptId,
        String postCode,
        String postCategory,
        String postName,
        Integer sortOrder,
        String status,
        String remarks) {}
