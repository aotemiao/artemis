package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;
import java.util.List;

/** 系统部门响应 DTO。 */
public record SystemDepartmentDTO(
        Long id,
        Long parentId,
        String ancestors,
        String deptName,
        String deptCategory,
        Integer sortOrder,
        Long leaderUserId,
        String phone,
        String email,
        String status,
        String remarks,
        List<SystemDepartmentDTO> children)
        implements Serializable {

    public SystemDepartmentDTO {
        children = children == null ? List.of() : List.copyOf(children);
    }

    @Override
    public List<SystemDepartmentDTO> children() {
        return List.copyOf(children);
    }
}
