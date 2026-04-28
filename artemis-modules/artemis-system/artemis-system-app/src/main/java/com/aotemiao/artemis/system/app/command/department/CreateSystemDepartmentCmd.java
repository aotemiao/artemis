package com.aotemiao.artemis.system.app.command.department;

import java.io.Serializable;

/** 新增系统部门命令。 */
public record CreateSystemDepartmentCmd(
        Long parentId,
        String deptName,
        String deptCategory,
        Integer sortOrder,
        Long leaderUserId,
        String phone,
        String email,
        String status,
        String remarks)
        implements Serializable {}
