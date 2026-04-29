package com.aotemiao.artemis.resource.infra.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.resource.domain.gateway.config.OssConfigGateway;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.resource.infra.gateway.config.OssConfigGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:resource_oss_config_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.resource.infra.repository"
        })
class OssConfigGatewayImplIntegrationTest {

    @Autowired
    private OssConfigGateway ossConfigGateway;

    @Test
    void save_thenFindAndPage_returnsConfig() {
        OssConfig saved = ossConfigGateway.save(sampleConfig("local", 1));

        assertThat(saved.getId()).isNotNull();
        assertThat(ossConfigGateway.findById(saved.getId())).isPresent();
        assertThat(ossConfigGateway.findByConfigKey("local")).isPresent();
        assertThat(ossConfigGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(OssConfig::getConfigKey)
                .contains("local");
    }

    @Test
    void clearDefaultExcept_keepsOnlySelectedDefault() {
        OssConfig first = ossConfigGateway.save(sampleConfig("local", 1));
        OssConfig second = ossConfigGateway.save(sampleConfig("aliyun", 1));

        ossConfigGateway.clearDefaultExcept(second.getId());

        assertThat(ossConfigGateway.findById(first.getId()))
                .get()
                .extracting(OssConfig::getDefaultFlag)
                .isEqualTo(0);
        assertThat(ossConfigGateway.findById(second.getId()))
                .get()
                .extracting(OssConfig::getDefaultFlag)
                .isEqualTo(1);
    }

    @Test
    void deleteById_softDeletesConfig() {
        OssConfig saved = ossConfigGateway.save(sampleConfig("delete", 0));

        ossConfigGateway.deleteById(saved.getId());

        assertThat(ossConfigGateway.findById(saved.getId())).isEmpty();
        assertThat(ossConfigGateway.findByConfigKey("delete")).isEmpty();
    }

    private static OssConfig sampleConfig(String configKey, Integer defaultFlag) {
        OssConfig config = new OssConfig();
        config.setConfigKey(configKey);
        config.setAccessKey("access");
        config.setSecretKey("secret");
        config.setBucket("bucket");
        config.setPrefix("prefix");
        config.setEndpoint("endpoint");
        config.setCustomDomain("cdn.example.com");
        config.setHttpsEnabled(true);
        config.setRegion("cn-hz");
        config.setAccessPolicy("PRIVATE");
        config.setStatus(1);
        config.setDefaultFlag(defaultFlag);
        config.setBuiltIn(0);
        config.setProvider("LOCAL");
        config.setExtJson("{}");
        return config;
    }
}
