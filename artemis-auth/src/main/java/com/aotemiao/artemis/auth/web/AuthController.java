package com.aotemiao.artemis.auth.web;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.auth.client.SystemClientValidateClient;
import com.aotemiao.artemis.auth.client.SystemLoginInfoRecordClient;
import com.aotemiao.artemis.auth.client.SystemUserAuthorizationClient;
import com.aotemiao.artemis.auth.client.SystemUserValidateClient;
import com.aotemiao.artemis.auth.web.dto.LoginResponse;
import com.aotemiao.artemis.system.client.dto.RecordLoginInfoRequest;
import com.aotemiao.artemis.system.client.dto.UserAuthorizationSnapshotDTO;
import com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
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
    private final SystemClientValidateClient systemClientValidateClient;
    private final SystemLoginInfoRecordClient systemLoginInfoRecordClient;

    public AuthController(
            SystemUserValidateClient systemUserValidateClient,
            SystemUserAuthorizationClient systemUserAuthorizationClient,
            SystemClientValidateClient systemClientValidateClient,
            SystemLoginInfoRecordClient systemLoginInfoRecordClient) {
        this.systemUserValidateClient = systemUserValidateClient;
        this.systemUserAuthorizationClient = systemUserAuthorizationClient;
        this.systemClientValidateClient = systemClientValidateClient;
        this.systemLoginInfoRecordClient = systemLoginInfoRecordClient;
    }

    /** 登录：校验用户名密码后签发 Token，会话存 Redis。 */
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody ValidateCredentialsRequest request, HttpServletRequest servletRequest) {
        if (!systemClientValidateClient.validate(request.clientId(), request.grantType())) {
            recordLoginInfo(request, servletRequest, "FAIL", "客户端或授权类型无效");
            throw new InvalidCredentialsException("Invalid client or grant type");
        }
        Long userId = systemUserValidateClient
                .validate(request.clientId(), request.grantType(), request.username(), request.password())
                .orElseThrow(() -> {
                    recordLoginInfo(request, servletRequest, "FAIL", "用户名或密码错误");
                    return new InvalidCredentialsException("Invalid username or password");
                });
        UserAuthorizationSnapshotDTO snapshot = getAuthorizationSnapshot(userId);
        StpUtil.login(userId);
        syncAuthorizationSession(snapshot);
        recordLoginInfo(request, servletRequest, "SUCCESS", "登录成功");
        return buildLoginResponse(snapshot);
    }

    /** 登出：使当前 Token 对应会话失效。 */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest servletRequest) {
        String username = currentUsername();
        recordLogoutInfo(username, servletRequest);
        StpUtil.logout();
    }

    /** 刷新：续期当前会话（Sa-Token 默认会续期），返回当前 Token。 若需续期后换新 Token，可在此扩展。 */
    @PostMapping("/refresh")
    public LoginResponse refresh() {
        StpUtil.checkLogin();
        Long userId = Long.parseLong(StpUtil.getLoginId().toString());
        UserAuthorizationSnapshotDTO snapshot = getAuthorizationSnapshot(userId);
        syncAuthorizationSession(snapshot);
        return buildLoginResponse(snapshot);
    }

    private UserAuthorizationSnapshotDTO getAuthorizationSnapshot(Long userId) {
        return systemUserAuthorizationClient
                .getByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Authorization snapshot not found: " + userId));
    }

    private LoginResponse buildLoginResponse(UserAuthorizationSnapshotDTO snapshot) {
        return new LoginResponse(
                StpUtil.getTokenValue(), snapshot.userId(), snapshot.roleKeys(), snapshot.permissionCodes());
    }

    /**
     * 将最小授权快照同步进登录会话，供 gateway 等后续请求复用。
     */
    private void syncAuthorizationSession(UserAuthorizationSnapshotDTO snapshot) {
        SaSession session = StpUtil.getSessionByLoginId(snapshot.userId());
        session.set(SaSession.ROLE_LIST, snapshot.roleKeys());
        session.set(SaSession.PERMISSION_LIST, snapshot.permissionCodes());

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("userId", snapshot.userId());
        userProfile.put("username", snapshot.username());
        userProfile.put("displayName", snapshot.displayName());
        session.set(SaSession.USER, userProfile);
    }

    private void recordLoginInfo(
            ValidateCredentialsRequest request, HttpServletRequest servletRequest, String status, String msg) {
        String userAgent = header(servletRequest, "User-Agent");
        systemLoginInfoRecordClient.record(new RecordLoginInfoRequest(
                header(servletRequest, "X-Tenant-Id"),
                request.username(),
                request.clientId(),
                null,
                clientIp(servletRequest),
                "未知",
                browser(userAgent),
                os(userAgent),
                status,
                msg));
    }

    private void recordLogoutInfo(String username, HttpServletRequest servletRequest) {
        String userAgent = header(servletRequest, "User-Agent");
        systemLoginInfoRecordClient.record(new RecordLoginInfoRequest(
                header(servletRequest, "X-Tenant-Id"),
                username,
                null,
                null,
                clientIp(servletRequest),
                "未知",
                browser(userAgent),
                os(userAgent),
                "SUCCESS",
                "登出成功"));
    }

    private String currentUsername() {
        if (!StpUtil.isLogin()) {
            return "anonymous";
        }
        Object userProfile = StpUtil.getSession().get(SaSession.USER);
        if (userProfile instanceof Map<?, ?> map && map.get("username") != null) {
            return map.get("username").toString();
        }
        return StpUtil.getLoginId().toString();
    }

    private static String header(HttpServletRequest request, String name) {
        return request == null ? null : request.getHeader(name);
    }

    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String browser(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("Edg/")) {
            return "Edge";
        }
        if (userAgent.contains("Chrome/")) {
            return "Chrome";
        }
        if (userAgent.contains("Firefox/")) {
            return "Firefox";
        }
        if (userAgent.contains("Safari/")) {
            return "Safari";
        }
        return "Unknown";
    }

    private static String os(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        if (userAgent.contains("Windows")) {
            return "Windows";
        }
        if (userAgent.contains("Mac OS X")) {
            return "macOS";
        }
        if (userAgent.contains("Linux")) {
            return "Linux";
        }
        return "Unknown";
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }
}
