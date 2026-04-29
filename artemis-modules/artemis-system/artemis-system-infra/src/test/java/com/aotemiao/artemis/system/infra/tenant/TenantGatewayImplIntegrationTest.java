package com.aotemiao.artemis.system.infra.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.tenant.TenantGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:tenant_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class TenantGatewayImplIntegrationTest {

    @Autowired
    private TenantGateway tenantGateway;

    @Test
    void save_thenFindById_returnsTenant() {
        Tenant saved = tenantGateway.save(sampleTenant("阿特米斯科技", "123456"));

        Optional<Tenant> found = tenantGateway.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCompanyName()).isEqualTo("阿特米斯科技");
        assertThat(found.get().getTenantNo()).isEqualTo("123456");
    }

    @Test
    void findPage_returnsTenants() {
        tenantGateway.save(sampleTenant("阿特米斯科技", "123456"));

        assertThat(tenantGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(Tenant::getCompanyName)
                .contains("阿特米斯科技");
    }

    @Test
    void findEnabled_onlyReturnsEnabledTenants() {
        Tenant enabled = sampleTenant("阿特米斯科技", "123456");
        enabled.setStatus("NORMAL");
        Tenant disabled = sampleTenant("测试停用", "654321");
        disabled.setStatus("DISABLED");
        tenantGateway.save(enabled);
        tenantGateway.save(disabled);

        assertThat(tenantGateway.findEnabled())
                .extracting(Tenant::getCompanyName)
                .containsExactly("阿特米斯科技");
    }

    @Test
    void deleteById_hidesTenantFromQueries() {
        Tenant saved = tenantGateway.save(sampleTenant("阿特米斯科技", "123456"));

        tenantGateway.deleteById(saved.getId());

        assertThat(tenantGateway.findById(saved.getId())).isEmpty();
        assertThat(tenantGateway.existsByCompanyName("阿特米斯科技", null)).isFalse();
    }

    private static Tenant sampleTenant(String companyName, String tenantNo) {
        Tenant tenant = new Tenant();
        tenant.setTenantNo(tenantNo);
        tenant.setCompanyName(companyName);
        tenant.setContactName("张三");
        tenant.setContactPhone("13800000000");
        tenant.setSocialCreditCode("91310000MA1");
        tenant.setAddress("上海市");
        tenant.setDomain("demo.artemis.com");
        tenant.setIntro("示例租户");
        tenant.setPackageId(1L);
        tenant.setExpireTime(LocalDateTime.parse("2030-01-01T00:00:00"));
        tenant.setUserLimit(100);
        tenant.setStatus("NORMAL");
        tenant.setRemarks("基础说明");
        return tenant;
    }
}
