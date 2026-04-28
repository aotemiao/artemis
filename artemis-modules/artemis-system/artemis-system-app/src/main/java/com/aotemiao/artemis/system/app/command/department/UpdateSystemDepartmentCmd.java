package com.aotemiao.artemis.system.app.command.department;

import java.io.Serializable;

/** 更新系统部门命令。 */
public record UpdateSystemDepartmentCmd(
        Long id,
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
