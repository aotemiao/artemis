package com.aotemiao.artemis.system.adapter.dubbo;

import com.aotemiao.artemis.system.app.query.GetUserAuthorizationQry;
import com.aotemiao.artemis.system.app.query.GetUserAuthorizationQryExe;
import com.aotemiao.artemis.system.client.api.UserAuthorizationService;
import com.aotemiao.artemis.system.client.dto.UserAuthorizationSnapshotDTO;
import java.util.Optional;
import org.apache.dubbo.config.annotation.DubboService;

/** 用户授权快照 Dubbo 服务实现，供内部调用方复用。 */
@DubboService
public class UserAuthorizationServiceDubboImpl implements UserAuthorizationService {

    private final GetUserAuthorizationQryExe getUserAuthorizationQryExe;

    public UserAuthorizationServiceDubboImpl(GetUserAuthorizationQryExe getUserAuthorizationQryExe) {
        this.getUserAuthorizationQryExe = getUserAuthorizationQryExe;
    }

    @Override
    public Optional<UserAuthorizationSnapshotDTO> getByUserId(Long userId) {
        return getUserAuthorizationQryExe
                .execute(new GetUserAuthorizationQry(userId))
                .map(snapshot -> new UserAuthorizationSnapshotDTO(
                        snapshot.userId(), snapshot.username(), snapshot.displayName(), snapshot.roleKeys()));
    }
}
