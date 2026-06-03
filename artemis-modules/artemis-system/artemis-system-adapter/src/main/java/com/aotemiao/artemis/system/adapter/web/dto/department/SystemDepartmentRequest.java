package com.aotemiao.artemis.system.adapter.web.dto.department;

import jakarta.validation.constraints.NotBlank;

/** 系统部门创建或更新请求。 */
public record SystemDepartmentRequest(
        Long parentId,
        @NotBlank String deptName,
        String deptCategory,
        Integer sortOrder,
        Long leaderUserId,
        String phone,
        String email,
        @NotBlank String status,
        String remarks) {}
