package com.aotemiao.artemis.system.domain.model.auth;

import java.io.Serializable;
import java.util.List;

/** 用户最小授权快照。 */
public record UserAuthorizationSnapshot(
        Long userId, String username, String displayName, List<String> roleKeys, List<String> permissionCodes)
        implements Serializable {

    public UserAuthorizationSnapshot(Long userId, String username, String displayName, List<String> roleKeys) {
        this(userId, username, displayName, roleKeys, List.of());
    }

    public UserAuthorizationSnapshot {
        roleKeys = roleKeys == null ? List.of() : List.copyOf(roleKeys);
        permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
    }
}
