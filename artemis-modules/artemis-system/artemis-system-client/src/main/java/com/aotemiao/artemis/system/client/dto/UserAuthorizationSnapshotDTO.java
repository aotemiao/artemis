package com.aotemiao.artemis.system.client.dto;

import java.io.Serializable;
import java.util.List;

/** 用户最小授权快照（供内部调用方复用）。 */
public record UserAuthorizationSnapshotDTO(
        Long userId, String username, String displayName, List<String> roleKeys, List<String> permissionCodes)
        implements Serializable {

    public UserAuthorizationSnapshotDTO(Long userId, String username, String displayName, List<String> roleKeys) {
        this(userId, username, displayName, roleKeys, List.of());
    }

    public UserAuthorizationSnapshotDTO {
        roleKeys = roleKeys == null ? List.of() : List.copyOf(roleKeys);
        permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
    }
}
