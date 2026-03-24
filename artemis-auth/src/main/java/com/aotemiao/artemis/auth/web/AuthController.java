package com.aotemiao.artemis.auth.web;

import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.auth.client.SystemUserAuthorizationClient;
import com.aotemiao.artemis.auth.client.SystemUserValidateClient;
import com.aotemiao.artemis.auth.web.dto.LoginResponse;
import com.aotemiao.artemis.system.client.dto.UserAuthorizationSnapshotDTO;
import com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 认证接口：登录、登出、Token 刷新。 路径以 /auth 为前缀，与网关路由一致。 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SystemUserValidateClient systemUserValidateClient;
    private final SystemUserAuthorizationClient systemUserAuthorizationClient;

    public AuthController(
            SystemUserValidateClient systemUserValidateClient,
            SystemUserAuthorizationClient systemUserAuthorizationClient) {
        this.systemUserValidateClient = systemUserValidateClient;
        this.systemUserAuthorizationClient = systemUserAuthorizationClient;
    }

    /** 登录：校验用户名密码后签发 Token，会话存 Redis。 */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody ValidateCredentialsRequest request) {
        Long userId = systemUserValidateClient
                .validate(request.username(), request.password())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        UserAuthorizationSnapshotDTO snapshot = getAuthorizationSnapshot(userId);
        StpUtil.login(userId);
        return new LoginResponse(StpUtil.getTokenValue(), snapshot.userId(), snapshot.roleKeys());
    }

    /** 登出：使当前 Token 对应会话失效。 */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        StpUtil.logout();
    }

    /** 刷新：续期当前会话（Sa-Token 默认会续期），返回当前 Token。 若需续期后换新 Token，可在此扩展。 */
    @PostMapping("/refresh")
    public LoginResponse refresh() {
        StpUtil.checkLogin();
        Long userId = Long.parseLong(StpUtil.getLoginId().toString());
        UserAuthorizationSnapshotDTO snapshot = getAuthorizationSnapshot(userId);
        return new LoginResponse(StpUtil.getTokenValue(), snapshot.userId(), snapshot.roleKeys());
    }

    private UserAuthorizationSnapshotDTO getAuthorizationSnapshot(Long userId) {
        return systemUserAuthorizationClient
                .getByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Authorization snapshot not found: " + userId));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}
