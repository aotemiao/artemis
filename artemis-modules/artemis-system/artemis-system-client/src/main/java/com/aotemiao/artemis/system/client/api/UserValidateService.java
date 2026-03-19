package com.aotemiao.artemis.system.client.api;

import com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest;
import java.util.Optional;

/**
 * 用户凭证校验服务（Dubbo 接口），供 artemis-auth 等内部调用方使用。 与 REST 契约 POST /api/system/internal/auth/validate 等价。
 */
public interface UserValidateService {

    /**
     * 校验用户名与密码，返回对应用户 ID。
     *
     * @param request 用户名与密码
     * @return 校验通过时返回 userId，否则 empty
     */
    Optional<Long> validate(ValidateCredentialsRequest request);
}
