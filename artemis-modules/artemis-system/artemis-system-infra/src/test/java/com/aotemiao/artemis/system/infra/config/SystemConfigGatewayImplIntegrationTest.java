package com.aotemiao.artemis.system.infra.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.config.SystemConfigGateway;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.config.SystemConfigGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_config_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class SystemConfigGatewayImplIntegrationTest {

    @Autowired
    private SystemConfigGateway systemConfigGateway;

    @Test
    void save_thenFindByIdAndKey_returnsSameData() {
        SystemConfig systemConfig = sampleConfig("sys.account.registerUser", "false", true);

        SystemConfig saved = systemConfigGateway.save(systemConfig);

        assertThat(saved.getId()).isNotNull();
        Optional<SystemConfig> foundById = systemConfigGateway.findById(saved.getId());
        Optional<SystemConfig> foundByKey = systemConfigGateway.findByConfigKey("sys.account.registerUser");
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getConfigValue()).isEqualTo("false");
        assertThat(foundByKey).isPresent();
        assertThat(foundByKey.get().isSystemBuiltIn()).isTrue();
    }

    @Test
    void findPage_returnsSavedConfigs() {
        systemConfigGateway.save(sampleConfig("sys.account.registerUser", "false", true));
        systemConfigGateway.save(sampleConfig("sys.user.initPassword", "123456", true));

        PageResult<SystemConfig> pageResult = systemConfigGateway.findPage(new PageRequest(0, 10));

        assertThat(pageResult.content()).hasSize(2);
        assertThat(pageResult.content().stream().map(SystemConfig::getConfigKey))
                .containsExactlyInAnyOrder("sys.account.registerUser", "sys.user.initPassword");
    }

    @Test
    void deleteById_hidesConfigFromQueries() {
        SystemConfig saved = systemConfigGateway.save(sampleConfig("demo.flag", "true", false));

        systemConfigGateway.deleteById(saved.getId());

        assertThat(systemConfigGateway.findById(saved.getId())).isEmpty();
        assertThat(systemConfigGateway.findByConfigKey("demo.flag")).isEmpty();
    }

    private static SystemConfig sampleConfig(String configKey, String configValue, boolean builtIn) {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setConfigName(configKey);
        systemConfig.setConfigKey(configKey);
        systemConfig.setConfigValue(configValue);
        systemConfig.setSystemBuiltIn(builtIn);
        systemConfig.setRemarks("remark");
        return systemConfig;
    }
}
