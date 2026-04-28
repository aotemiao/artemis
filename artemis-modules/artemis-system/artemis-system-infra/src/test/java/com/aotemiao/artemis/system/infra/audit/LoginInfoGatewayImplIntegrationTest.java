package com.aotemiao.artemis.system.infra.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.audit.LoginInfoGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:login_info_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class LoginInfoGatewayImplIntegrationTest {

    @Autowired
    private LoginInfoGateway loginInfoGateway;

    @Test
    void save_thenFindPageAndDelete_returnsExpectedData() {
        LoginInfo saved = loginInfoGateway.save(sampleLoginInfo("admin"));

        assertThat(loginInfoGateway.findById(saved.getId())).isPresent();
        assertThat(loginInfoGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(LoginInfo::getUsername)
                .containsExactly("admin");

        loginInfoGateway.deleteByIds(java.util.List.of(saved.getId()));

        assertThat(loginInfoGateway.findById(saved.getId())).isEmpty();
    }

    @Test
    void clear_hidesAllRecords() {
        loginInfoGateway.save(sampleLoginInfo("admin"));
        loginInfoGateway.save(sampleLoginInfo("demo"));

        loginInfoGateway.clear();

        assertThat(loginInfoGateway.findPage(new PageRequest(0, 10)).content()).isEmpty();
    }

    private static LoginInfo sampleLoginInfo(String username) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setTenantId("000000");
        loginInfo.setUsername(username);
        loginInfo.setClientId("artemis-admin");
        loginInfo.setDeviceType("PC");
        loginInfo.setIpaddr("127.0.0.1");
        loginInfo.setLoginLocation("未知");
        loginInfo.setBrowser("Chrome");
        loginInfo.setOs("Windows");
        loginInfo.setStatus("SUCCESS");
        loginInfo.setMsg("登录成功");
        loginInfo.setLoginTime(LocalDateTime.now());
        return loginInfo;
    }
}
