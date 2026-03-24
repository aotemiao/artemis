package com.aotemiao.artemis.auth.client;

import com.aotemiao.artemis.system.client.api.UserAuthorizationService;
import com.aotemiao.artemis.system.client.dto.UserAuthorizationSnapshotDTO;
import java.util.Optional;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 调用 artemis-system 的用户授权快照能力（通过 Dubbo）。 */
@Component
public class SystemUserAuthorizationClient {

    @DubboReference
    private UserAuthorizationService userAuthorizationService;

    /**
     * 按用户 ID 查询授权快照。
     *
     * @param userId 用户 ID
     * @return 找到用户时返回授权快照，否则 empty
     */
    public Optional<UserAuthorizationSnapshotDTO> getByUserId(Long userId) {
        return userAuthorizationService.getByUserId(userId);
    }
}
