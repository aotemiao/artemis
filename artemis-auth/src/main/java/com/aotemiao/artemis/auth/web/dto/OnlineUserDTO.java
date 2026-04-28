package com.aotemiao.artemis.auth.web.dto;

import java.time.LocalDateTime;

public record OnlineUserDTO(
        Long userId,
        String username,
        String token,
        String ipaddr,
        String browser,
        String os,
        LocalDateTime loginTime) {}
