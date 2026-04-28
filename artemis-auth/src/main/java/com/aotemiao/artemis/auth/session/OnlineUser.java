package com.aotemiao.artemis.auth.session;

import java.time.LocalDateTime;

public record OnlineUser(
        Long userId,
        String username,
        String token,
        String ipaddr,
        String browser,
        String os,
        LocalDateTime loginTime) {}
