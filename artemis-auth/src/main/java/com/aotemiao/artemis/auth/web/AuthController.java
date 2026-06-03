package com.aotemiao.artemis.auth.web;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.auth.client.SystemClientValidateClient;
import com.aotemiao.artemis.auth.client.SystemLoginInfoRecordClient;
import com.aotemiao.artemis.auth.client.SystemUserAuthorizationClient;
import com.aotemiao.artemis.auth.client.SystemUserRegisterClient;
import com.aotemiao.artemis.auth.client.SystemUserValidateClient;
import com.aotemiao.artemis.auth.session.OnlineUser;
import com.aotemiao.artemis.auth.session.OnlineUserRegistry;
import com.aotemiao.artemis.auth.web.dto.LoginResponse;
import com.aotemiao.artemis.auth.web.dto.OnlineUserDTO;
import com.aotemiao.artemis.auth.web.dto.RegisterResponse;
import com.aotemiao.artemis.system.client.dto.audit.RecordLoginInfoRequest;
import com.aotemiao.artemis.system.client.dto.auth.RegisterUserRequest;
import com.aotemiao.artemis.system.client.dto.auth.UserAuthorizationSnapshotDTO;
import com.aotemiao.artemis.system.client.dto.auth.ValidateCredentialsRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 认证接口：登录、登出、Token 刷新。 路径以 /auth 为前缀，与网关路由一致。 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String DYNAMIC_TENANT_ID = "dynamicTenantId";

    private final SystemUserValidateClient systemUserValidateClient;
    private final SystemUserAuthorizationClient systemUserAuthorizationClient;
    private final SystemClientValidateClient systemClientValidateClient;
    private final SystemLoginInfoRecordClient systemLoginInfoRecordClient;
    private final SystemUserRegisterClient systemUserRegisterClient;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring 注入在线用户注册表作为受管协作者，控制器不会向外暴露该引用。")
    private final OnlineUserRegistry onlineUserRegistry;

    public AuthController(
            SystemUserValidateClient systemUserValidateClient,
            SystemUserAuthorizationClient systemUserAuthorizationClient,
            SystemClientValidateClient systemClientValidateClient,
            SystemLoginInfoRecordClient systemLoginInfoRecordClient,
            SystemUserRegisterClient systemUserRegisterClient,
            OnlineUserRegistry onlineUserRegistry) {
        this.systemUserValidateClient = systemUserValidateClient;
        this.systemUserAuthorizationClient = systemUserAuthorizationClient;
        this.systemClientValidateClient = systemClientValidateClient;
        this.systemLoginInfoRecordClient = systemLoginInfoRecordClient;
        this.systemUserRegisterClient = systemUserRegisterClient;
        this.onlineUserRegistry = onlineUserRegistry;
    }

    /** 登录：校验用户名密码后签发 Token，会话存 Redis。 */
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody ValidateCredentialsRequest request, HttpServletRequest servletRequest) {
        String tenantId = resolveTenantId(request.tenantId(), servletRequest);
        if (!systemClientValidateClient.validate(request.clientId(), request.grantType())) {
            recordLoginInfo(tenantId, request, servletRequest, "FAIL", "客户端或授权类型无效");
            throw new InvalidCredentialsException("Invalid client or grant type");
        }
        Long userId = systemUserValidateClient
                .validate(tenantId, request.clientId(), request.grantType(), request.username(), request.password())
                .orElseThrow(() -> {
                    recordLoginInfo(tenantId, request, servletRequest, "FAIL", "用户名或密码错误");
                    return new InvalidCredentialsException("Invalid username or password");
                });
        UserAuthorizationSnapshotDTO snapshot = getAuthorizationSnapshot(userId);
        StpUtil.login(userId);
        syncAuthorizationSession(snapshot);
        recordOnlineUser(snapshot, servletRequest);
        recordLoginInfo(tenantId, request, servletRequest, "SUCCESS", "登录成功");
        return buildLoginResponse(snapshot);
    }

    /** 注册：开关、用户类型和唯一性由系统服务统一校验。 */
    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterUserRequest request, HttpServletRequest servletRequest) {
        if (!systemClientValidateClient.validate(request.clientId(), request.grantType())) {
            recordRegisterInfo(request, servletRequest, "FAIL", "客户端或授权类型无效");
            throw new InvalidCredentialsException("Invalid client or grant type");
        }
        try {
            Long userId = systemUserRegisterClient.register(request);
            recordRegisterInfo(request, servletRequest, "SUCCESS", "注册成功");
            return new RegisterResponse(userId, request.username());
        } catch (RuntimeException ex) {
            recordRegisterInfo(request, servletRequest, "FAIL", "注册失败：" + ex.getMessage());
            throw ex;
        }
    }

    /** 登出：使当前 Token 对应会话失效。 */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest servletRequest) {
        String username = currentUsername();
        recordLogoutInfo(username, servletRequest);
        if (StpUtil.isLogin()) {
            onlineUserRegistry.remove(Long.parseLong(StpUtil.getLoginId().toString()));
        }
        StpUtil.logout();
    }

    /** 在线用户：按用户名或 IP 过滤当前认证实例内的登录用户。 */
    @GetMapping("/online-users")
    public List<OnlineUserDTO> onlineUsers(
            @RequestParam(required = false) String username, @RequestParam(required = false) String ipaddr) {
        return onlineUserRegistry.list(username, ipaddr).stream()
                .map(this::toOnlineUserDTO)
                .toList();
    }

    /** 强退指定用户。 */
    @PostMapping("/online-users/{userId}/force-logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forceLogout(@PathVariable Long userId) {
        StpUtil.logout(userId);
        onlineUserRegistry.remove(userId);
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

    /** 超级管理员动态切换租户。 */
    @PostMapping("/tenant/switch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void switchTenant(@RequestParam String tenantId) {
        StpUtil.checkLogin();
        ensureSuperAdmin();
        StpUtil.getSession().set(DYNAMIC_TENANT_ID, tenantId);
    }

    /** 超级管理员清除动态租户。 */
    @PostMapping("/tenant/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearTenant() {
        StpUtil.checkLogin();
        ensureSuperAdmin();
        StpUtil.getSession().delete(DYNAMIC_TENANT_ID);
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

    private void recordOnlineUser(UserAuthorizationSnapshotDTO snapshot, HttpServletRequest servletRequest) {
        String userAgent = header(servletRequest, "User-Agent");
        onlineUserRegistry.put(new OnlineUser(
                snapshot.userId(),
                snapshot.username(),
                StpUtil.getTokenValue(),
                clientIp(servletRequest),
                browser(userAgent),
                os(userAgent),
                LocalDateTime.now()));
    }

    private OnlineUserDTO toOnlineUserDTO(OnlineUser onlineUser) {
        return new OnlineUserDTO(
                onlineUser.userId(),
                onlineUser.username(),
                onlineUser.token(),
                onlineUser.ipaddr(),
                onlineUser.browser(),
                onlineUser.os(),
                onlineUser.loginTime());
    }

    private void recordLoginInfo(
            String tenantId,
            ValidateCredentialsRequest request,
            HttpServletRequest servletRequest,
            String status,
            String msg) {
        String userAgent = header(servletRequest, "User-Agent");
        systemLoginInfoRecordClient.record(new RecordLoginInfoRequest(
                tenantId,
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

    private void recordRegisterInfo(
            RegisterUserRequest request, HttpServletRequest servletRequest, String status, String msg) {
        String userAgent = header(servletRequest, "User-Agent");
        systemLoginInfoRecordClient.record(new RecordLoginInfoRequest(
                request.tenantId(),
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

    private static String resolveTenantId(String requestedTenantId, HttpServletRequest request) {
        if (requestedTenantId != null && !requestedTenantId.isBlank()) {
            return requestedTenantId;
        }
        String headerTenantId = header(request, "X-Tenant-Id");
        if (headerTenantId != null && !headerTenantId.isBlank()) {
            return headerTenantId;
        }
        if (StpUtil.isLogin()) {
            Object dynamicTenantId = StpUtil.getSession().get(DYNAMIC_TENANT_ID);
            if (dynamicTenantId != null) {
                return dynamicTenantId.toString();
            }
        }
        return null;
    }

    private static void ensureSuperAdmin() {
        Object roles = StpUtil.getSession().get(SaSession.ROLE_LIST);
        if (!(roles instanceof List<?> roleList) || roleList.stream().noneMatch("super-admin"::equals)) {
            throw new ForbiddenOperationException("Only super admin can switch tenant");
        }
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

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class ForbiddenOperationException extends RuntimeException {
        public ForbiddenOperationException(String message) {
            super(message);
        }
    }
}
