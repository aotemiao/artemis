package com.aotemiao.artemis.system.domain.model;

import java.io.Serializable;
import java.util.List;

/** 用户最小授权快照。 */
public record UserAuthorizationSnapshot(Long userId, String username, String displayName, List<String> roleKeys)
        implements Serializable {

    public UserAuthorizationSnapshot {
        roleKeys = roleKeys == null ? List.of() : List.copyOf(roleKeys);
    }
}
