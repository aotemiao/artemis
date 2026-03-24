package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;

/** 系统角色响应 DTO。 */
public record SystemRoleDTO(Long id, String roleKey, String roleName, Boolean enabled) implements Serializable {}
