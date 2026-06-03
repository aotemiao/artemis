package com.aotemiao.artemis.system.adapter.web.dto.user;

import java.io.Serializable;

/** 系统用户 DTO。 */
public record SystemUserDTO(Long id, String tenantNo, String username, String displayName, Boolean enabled)
        implements Serializable {}
