package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;

/** 系统岗位响应 DTO。 */
public record SystemPostDTO(
        Long id,
        Long deptId,
        String postCode,
        String postCategory,
        String postName,
        Integer sortOrder,
        String status,
        String remarks)
        implements Serializable {}
