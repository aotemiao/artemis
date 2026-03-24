package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;

/** 系统用户 DTO。 */
public record SystemUserDTO(Long id, String username, String displayName, Boolean enabled) implements Serializable {}
