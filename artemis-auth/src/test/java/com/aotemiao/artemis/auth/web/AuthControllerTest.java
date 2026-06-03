package com.aotemiao.artemis.auth.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
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
import com.aotemiao.artemis.system.client.dto.audit.RecordLoginInfoRequest;
import com.aotemiao.artemis.system.client.dto.auth.RegisterUserRequest;
import com.aotemiao.artemis.system.client.dto.auth.UserAuthorizationSnapshotDTO;
import com.aotemiao.artemis.system.client.dto.auth.ValidateCredentialsRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private SystemUserValidateClient systemUserValidateClient;

    @Mock
    private SystemUserAuthorizationClient systemUserAuthorizationClient;

    @Mock
    private SystemClientValidateClient systemClientValidateClient;

    @Mock
    private SystemLoginInfoRecordClient systemLoginInfoRecordClient;

    @Mock
    private SystemUserRegisterClient systemUserRegisterClient;

    @Mock
    private OnlineUserRegistry onlineUserRegistry;

    @InjectMocks
    private AuthController authController;

    private SaTokenContext previousSaTokenContext;

    @BeforeEach
    void setUp() {
        previousSaTokenContext = SaManager.getSaTokenContext();
        SaManager.setSaTokenContext(new TestSaTokenContext());
    }

    @AfterEach
    void tearDown() {
        SaManager.setSaTokenContext(previousSaTokenContext);
    }

    @Test
    void login_whenCredentialsInvalid_throwsInvalidCredentialsException() {
        when(systemClientValidateClient.validate("artemis-admin", "password")).thenReturn(true);
        when(systemUserValidateClient.validate(null, "artemis-admin", "password", "admin", "bad-password"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(
                        new ValidateCredentialsRequest("admin", "bad-password"),
                        org.mockito.Mockito.mock(HttpServletRequest.class)))
                .isInstanceOf(AuthController.InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void login_whenCredentialsValid_returnsTokenAndRoleKeys() {
        when(systemClientValidateClient.validate("artemis-admin", "password")).thenReturn(true);
        when(systemUserValidateClient.validate("100001", "artemis-admin", "password", "admin", "123456"))
                .thenReturn(Optional.of(1L));
        when(systemUserAuthorizationClient.getByUserId(1L))
                .thenReturn(Optional.of(new UserAuthorizationSnapshotDTO(
                        1L, "admin", "管理员", List.of("super-admin"), List.of("system:user:list"))));

        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(request.getHeader(anyString())).thenAnswer(invocation -> switch (invocation.getArgument(0, String.class)) {
            case "X-Tenant-Id" -> "100001";
            case "User-Agent" ->
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36";
            default -> null;
        });
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        LoginResponse response = authController.login(new ValidateCredentialsRequest("admin", "123456"), request);

        assertThat(response.token()).isNotBlank();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.roleKeys()).containsExactly("super-admin");
        assertThat(response.permissionCodes()).containsExactly("system:user:list");
        assertThat(StpUtil.getSessionByLoginId(1L).get(SaSession.ROLE_LIST)).isEqualTo(List.of("super-admin"));
        assertThat(StpUtil.getSessionByLoginId(1L).get(SaSession.PERMISSION_LIST))
                .isEqualTo(List.of("system:user:list"));
        Object userProfile = StpUtil.getSessionByLoginId(1L).get(SaSession.USER);
        assertThat(userProfile).isInstanceOf(Map.class);
        Map<?, ?> userProfileMap = (Map<?, ?>) userProfile;
        assertThat(userProfileMap.get("userId")).isEqualTo(1L);
        assertThat(userProfileMap.get("username")).isEqualTo("admin");
        assertThat(userProfileMap.get("displayName")).isEqualTo("管理员");
        org.mockito.Mockito.verify(onlineUserRegistry)
                .put(org.mockito.ArgumentMatchers.argThat(user -> user.userId().equals(1L)
                        && "admin".equals(user.username())
                        && "Chrome".equals(user.browser())));
        org.mockito.Mockito.verify(systemLoginInfoRecordClient)
                .record(org.mockito.ArgumentMatchers.argThat(AuthControllerTest::successLoginInfo));
    }

    @Test
    void login_whenClientInvalid_throwsInvalidCredentialsException() {
        when(systemClientValidateClient.validate("bad-client", "password")).thenReturn(false);

        assertThatThrownBy(() -> authController.login(
                        new ValidateCredentialsRequest("bad-client", "password", "admin", "123456"),
                        org.mockito.Mockito.mock(HttpServletRequest.class)))
                .isInstanceOf(AuthController.InvalidCredentialsException.class)
                .hasMessage("Invalid client or grant type");
    }

    @Test
    void register_whenAllowed_returnsUserIdAndRecordsAudit() {
        RegisterUserRequest registerRequest =
                new RegisterUserRequest("000000", "artemis-admin", "password", "demo", "123456", "SYSTEM");
        when(systemClientValidateClient.validate("artemis-admin", "password")).thenReturn(true);
        when(systemUserRegisterClient.register(registerRequest)).thenReturn(2L);

        var response = authController.register(registerRequest, org.mockito.Mockito.mock(HttpServletRequest.class));

        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.username()).isEqualTo("demo");
        org.mockito.Mockito.verify(systemLoginInfoRecordClient)
                .record(org.mockito.ArgumentMatchers.argThat(
                        request -> "demo".equals(request.username()) && "SUCCESS".equals(request.status())));
    }

    @Test
    void refresh_whenLoggedIn_returnsTokenAndRoleKeys() {
        when(systemUserAuthorizationClient.getByUserId(1L))
                .thenReturn(Optional.of(new UserAuthorizationSnapshotDTO(
                        1L, "admin", "管理员", List.of("super-admin"), List.of("system:user:list"))));

        StpUtil.login(1L);
        StpUtil.getSessionByLoginId(1L).set(SaSession.ROLE_LIST, List.of("legacy-role"));
        LoginResponse response = authController.refresh();

        assertThat(response.token()).isNotBlank();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.roleKeys()).containsExactly("super-admin");
        assertThat(response.permissionCodes()).containsExactly("system:user:list");
        assertThat(StpUtil.getSessionByLoginId(1L).get(SaSession.ROLE_LIST)).isEqualTo(List.of("super-admin"));
        assertThat(StpUtil.getSessionByLoginId(1L).get(SaSession.PERMISSION_LIST))
                .isEqualTo(List.of("system:user:list"));
    }

    @Test
    void onlineUsers_returnsFilteredRegistryUsers() {
        OnlineUser onlineUser =
                new OnlineUser(1L, "admin", "token", "127.0.0.1", "Chrome", "Windows", LocalDateTime.now());
        when(onlineUserRegistry.list("admin", "127.0.0.1")).thenReturn(List.of(onlineUser));

        var result = authController.onlineUsers("admin", "127.0.0.1");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().username()).isEqualTo("admin");
    }

    @Test
    void switchTenant_whenSuperAdmin_setsDynamicTenant() {
        StpUtil.login(1L);
        StpUtil.getSession().set(SaSession.ROLE_LIST, List.of("super-admin"));

        authController.switchTenant("100001");

        assertThat(StpUtil.getSession().get("dynamicTenantId")).isEqualTo("100001");
    }

    @Test
    void clearTenant_whenSuperAdmin_removesDynamicTenant() {
        StpUtil.login(1L);
        StpUtil.getSession().set(SaSession.ROLE_LIST, List.of("super-admin"));
        StpUtil.getSession().set("dynamicTenantId", "100001");

        authController.clearTenant();

        assertThat(StpUtil.getSession().get("dynamicTenantId")).isNull();
    }

    @Test
    void switchTenant_whenNotSuperAdmin_throwsForbidden() {
        StpUtil.login(1L);
        StpUtil.getSession().set(SaSession.ROLE_LIST, List.of("tenant-admin"));

        assertThatThrownBy(() -> authController.switchTenant("100001"))
                .isInstanceOf(AuthController.ForbiddenOperationException.class)
                .hasMessage("Only super admin can switch tenant");
    }

    private static boolean successLoginInfo(RecordLoginInfoRequest request) {
        return "admin".equals(request.username())
                && "artemis-admin".equals(request.clientId())
                && "SUCCESS".equals(request.status())
                && "Chrome".equals(request.browser())
                && "Windows".equals(request.os());
    }

    private static final class TestSaTokenContext implements SaTokenContext {

        private final SaRequest request = new TestSaRequest();
        private final SaResponse response = new TestSaResponse();
        private final SaStorage storage = new TestSaStorage();

        @Override
        public SaRequest getRequest() {
            return request;
        }

        @Override
        public SaResponse getResponse() {
            return response;
        }

        @Override
        public SaStorage getStorage() {
            return storage;
        }

        @Override
        public boolean matchPath(String pattern, String path) {
            return pattern != null && pattern.equals(path);
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

    private static final class TestSaStorage implements SaStorage {

        private final Map<String, Object> data = new HashMap<>();

        @Override
        public Object getSource() {
            return data;
        }

        @Override
        public Object get(String key) {
            return data.get(key);
        }

        @Override
        public SaStorage set(String key, Object value) {
            data.put(key, value);
            return this;
        }

        @Override
        public SaStorage delete(String key) {
            data.remove(key);
            return this;
        }
    }

    private static final class TestSaRequest implements SaRequest {

        @Override
        public Object getSource() {
            return this;
        }

        @Override
        public String getParam(String name) {
            return null;
        }

        @Override
        public List<String> getParamNames() {
            return Collections.emptyList();
        }

        @Override
        public Map<String, String> getParamMap() {
            return Collections.emptyMap();
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public String getCookieValue(String name) {
            return null;
        }

        @Override
        public String getCookieFirstValue(String name) {
            return null;
        }

        @Override
        public String getCookieLastValue(String name) {
            return null;
        }

        @Override
        public String getRequestPath() {
            return "/";
        }

        @Override
        public String getUrl() {
            return "/";
        }

        @Override
        public String getMethod() {
            return "POST";
        }

        @Override
        public Object forward(String path) {
            return null;
        }
    }

    private static final class TestSaResponse implements SaResponse {

        @Override
        public Object getSource() {
            return this;
        }

        @Override
        public SaResponse setStatus(int sc) {
            return this;
        }

        @Override
        public SaResponse setHeader(String name, String value) {
            return this;
        }

        @Override
        public SaResponse addHeader(String name, String value) {
            return this;
        }

        @Override
        public Object redirect(String url) {
            return null;
        }
    }
}
