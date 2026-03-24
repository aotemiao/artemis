package com.aotemiao.artemis.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.aotemiao.artemis.gateway.config.SessionBackedStpInterface;
import com.aotemiao.artemis.gateway.support.SaTokenTestSupport;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class RoleKeysHeaderGatewayFilterTest {

    private final RoleKeysHeaderGatewayFilter roleKeysHeaderGatewayFilter = new RoleKeysHeaderGatewayFilter();

    private SaTokenContext previousSaTokenContext;
    private StpInterface previousStpInterface;

    @BeforeEach
    void setUp() {
        previousSaTokenContext = SaTokenTestSupport.installContext();
        previousStpInterface = SaManager.getStpInterface();
        SaManager.setStpInterface(new SessionBackedStpInterface());
    }

    @AfterEach
    void tearDown() {
        SaManager.setSaTokenContext(previousSaTokenContext);
        SaManager.setStpInterface(previousStpInterface);
    }

    @Test
    void filter_whenAuthenticated_shouldInjectRoleHeader() {
        StpUtil.login(1L);
        StpUtil.getSessionByLoginId(1L).set(SaSession.ROLE_LIST, List.of("super-admin", "auditor"));

        CapturingGatewayFilterChain gatewayFilterChain = new CapturingGatewayFilterChain();
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/system/users/1").build());

        roleKeysHeaderGatewayFilter.filter(exchange, gatewayFilterChain).block();

        assertThat(gatewayFilterChain
                        .getExchange()
                        .getRequest()
                        .getHeaders()
                        .getFirst(RoleKeysHeaderGatewayFilter.X_ROLE_KEYS))
                .isEqualTo("super-admin,auditor");
    }

    @Test
    void filter_whenWhitelistPath_shouldNotInjectRoleHeader() {
        StpUtil.login(1L);
        StpUtil.getSessionByLoginId(1L).set(SaSession.ROLE_LIST, List.of("super-admin"));

        CapturingGatewayFilterChain gatewayFilterChain = new CapturingGatewayFilterChain();
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/auth/refresh").build());

        roleKeysHeaderGatewayFilter.filter(exchange, gatewayFilterChain).block();

        assertThat(gatewayFilterChain
                        .getExchange()
                        .getRequest()
                        .getHeaders()
                        .containsKey(RoleKeysHeaderGatewayFilter.X_ROLE_KEYS))
                .isFalse();
    }

    private static final class CapturingGatewayFilterChain implements GatewayFilterChain {

        private ServerWebExchange exchange;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.exchange = exchange;
            return Mono.empty();
        }

        public ServerWebExchange getExchange() {
            return exchange;
        }
    }
}
