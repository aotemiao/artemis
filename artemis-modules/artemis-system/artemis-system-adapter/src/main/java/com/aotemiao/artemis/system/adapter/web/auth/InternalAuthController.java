package com.aotemiao.artemis.system.adapter.web.auth;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.auth.ValidateCredentialsCmd;
import com.aotemiao.artemis.system.app.command.auth.ValidateCredentialsCmdExe;
import com.aotemiao.artemis.system.app.query.auth.GetUserAuthorizationQry;
import com.aotemiao.artemis.system.app.query.auth.GetUserAuthorizationQryExe;
import com.aotemiao.artemis.system.app.query.client.ValidateSystemClientQry;
import com.aotemiao.artemis.system.app.query.client.ValidateSystemClientQryExe;
import com.aotemiao.artemis.system.client.dto.auth.UserAuthorizationSnapshotDTO;
import com.aotemiao.artemis.system.client.dto.auth.ValidateCredentialsRequest;
import com.aotemiao.artemis.system.client.dto.client.ValidateClientRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部认证校验接口，供 artemis-auth 调用。 契约见 `artemis-system-client` 与仓库内 API 文档。
 */
@RestController
@RequestMapping(InternalAuthController.BASE_PATH)
public class InternalAuthController {

    public static final String BASE_PATH = "/api/system/internal/auth";

    private final ValidateCredentialsCmdExe validateCredentialsCmdExe;
    private final GetUserAuthorizationQryExe getUserAuthorizationQryExe;
    private final ValidateSystemClientQryExe validateSystemClientQryExe;

    public InternalAuthController(
            ValidateCredentialsCmdExe validateCredentialsCmdExe,
            GetUserAuthorizationQryExe getUserAuthorizationQryExe,
            ValidateSystemClientQryExe validateSystemClientQryExe) {
        this.validateCredentialsCmdExe = validateCredentialsCmdExe;
        this.getUserAuthorizationQryExe = getUserAuthorizationQryExe;
        this.validateSystemClientQryExe = validateSystemClientQryExe;
    }

    /** 校验用户名与密码，返回用户 ID。 校验失败返回 401。 */
    @PostMapping("/validate")
    public R<Long> validate(@Valid @RequestBody ValidateCredentialsRequest request) {
        var cmd = new ValidateCredentialsCmd(
                request.tenantId(), request.clientId(), request.grantType(), request.username(), request.password());
        Long userId = validateCredentialsCmdExe
                .execute(cmd)
                .orElseThrow(() -> new BizException(CommonErrorCode.UNAUTHORIZED, "Invalid username or password"));
        return R.ok(userId);
    }

    /** 校验客户端存在、状态正常且支持指定授权类型。 */
    @PostMapping("/clients/validate")
    public R<Boolean> validateClient(@Valid @RequestBody ValidateClientRequest request) {
        boolean allowed = validateSystemClientQryExe.execute(
                new ValidateSystemClientQry(request.clientId(), request.grantType()));
        if (!allowed) {
            throw new BizException(CommonErrorCode.UNAUTHORIZED, "Invalid client or grant type");
        }
        return R.ok(true);
    }

    /** 按用户 ID 查询最小授权快照。 */
    @GetMapping("/users/{userId}/authorization")
    public R<UserAuthorizationSnapshotDTO> getAuthorization(@PathVariable Long userId) {
        UserAuthorizationSnapshotDTO snapshot = getUserAuthorizationQryExe
                .execute(new GetUserAuthorizationQry(userId))
                .map(result -> new UserAuthorizationSnapshotDTO(
                        result.userId(),
                        result.username(),
                        result.displayName(),
                        result.roleKeys(),
                        result.permissionCodes()))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemUser not found: " + userId));
        return R.ok(snapshot);
    }
}
