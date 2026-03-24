package com.aotemiao.artemis.system.client.api;

import com.aotemiao.artemis.system.client.dto.UserAuthorizationSnapshotDTO;
import java.util.Optional;

/**
 * 用户授权快照服务（Dubbo 接口），供 artemis-auth 等内部调用方使用。 与 REST 契约
 * GET /api/system/internal/auth/users/{userId}/authorization 等价。
 */
public interface UserAuthorizationService {

    /**
     * 按用户 ID 查询最小授权快照。
     *
     * @param userId 用户 ID
     * @return 找到用户时返回授权快照，否则 empty
     */
    Optional<UserAuthorizationSnapshotDTO> getByUserId(Long userId);
}
