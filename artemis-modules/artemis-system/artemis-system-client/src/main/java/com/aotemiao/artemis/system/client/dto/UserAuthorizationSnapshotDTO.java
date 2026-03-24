package com.aotemiao.artemis.system.client.dto;

import java.io.Serializable;
import java.util.List;

/** 用户最小授权快照（供内部调用方复用）。 */
public record UserAuthorizationSnapshotDTO(Long userId, String username, String displayName, List<String> roleKeys)
        implements Serializable {

    public UserAuthorizationSnapshotDTO {
        roleKeys = roleKeys == null ? List.of() : List.copyOf(roleKeys);
    }
}
