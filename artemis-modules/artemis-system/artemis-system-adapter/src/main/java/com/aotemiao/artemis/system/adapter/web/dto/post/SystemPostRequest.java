package com.aotemiao.artemis.system.adapter.web.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 系统岗位请求。 */
public record SystemPostRequest(
        @NotNull Long deptId,
        @NotBlank String postCode,
        String postCategory,
        @NotBlank String postName,
        @NotNull Integer sortOrder,
        @NotBlank String status,
        String remarks) {}
