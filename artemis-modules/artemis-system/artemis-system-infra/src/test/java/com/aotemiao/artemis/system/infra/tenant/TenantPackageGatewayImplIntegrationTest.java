package com.aotemiao.artemis.system.infra.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.tenant.TenantPackageGatewayImpl.class,
    com.aotemiao.artemis.system.infra.gateway.tenant.TenantGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:tenant_package_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class TenantPackageGatewayImplIntegrationTest {

    @Autowired
    private TenantPackageGateway tenantPackageGateway;

    @Autowired
    private TenantGateway tenantGateway;

    @Test
    void save_thenFindById_returnsPackageWithMenus() {
        TenantPackage saved = tenantPackageGateway.save(samplePackage("标准版", true, List.of(1L, 2L)));

        Optional<TenantPackage> found = tenantPackageGateway.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPackageName()).isEqualTo("标准版");
        assertThat(found.get().getMenuIds()).containsExactly(1L, 2L);
    }

    @Test
    void findPage_returnsPackages() {
        tenantPackageGateway.save(samplePackage("标准版", true, List.of(1L)));

        assertThat(tenantPackageGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(TenantPackage::getPackageName)
                .contains("标准版");
    }

    @Test
    void findEnabled_onlyReturnsEnabledPackages() {
        tenantPackageGateway.save(samplePackage("标准版", true, List.of(1L)));
        tenantPackageGateway.save(samplePackage("停用版", false, List.of(2L)));

        assertThat(tenantPackageGateway.findEnabled())
                .extracting(TenantPackage::getPackageName)
                .containsExactly("标准版");
    }

    @Test
    void deleteById_hidesPackageFromQueries() {
        TenantPackage saved = tenantPackageGateway.save(samplePackage("标准版", true, List.of(1L)));

        tenantPackageGateway.deleteById(saved.getId());

        assertThat(tenantPackageGateway.findById(saved.getId())).isEmpty();
        assertThat(tenantPackageGateway.existsByPackageName("标准版", null)).isFalse();
    }

    @Test
    void isUsedByTenant_detectsTenantReference() {
        TenantPackage saved = tenantPackageGateway.save(samplePackage("标准版", true, List.of(1L)));
        tenantGateway.save(sampleTenant(saved.getId()));

        assertThat(tenantPackageGateway.isUsedByTenant(saved.getId())).isTrue();
    }

    private static TenantPackage samplePackage(String packageName, boolean enabled, List<Long> menuIds) {
        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.setPackageName(packageName);
        tenantPackage.setMenuCheckStrictly(true);
        tenantPackage.setEnabled(enabled);
        tenantPackage.setRemarks("测试套餐");
        tenantPackage.setMenuIds(menuIds);
        return tenantPackage;
    }

    private static Tenant sampleTenant(Long packageId) {
        Tenant tenant = new Tenant();
        tenant.setTenantNo("123456");
        tenant.setCompanyName("阿特米斯科技");
        tenant.setContactName("张三");
        tenant.setContactPhone("13800000000");
        tenant.setSocialCreditCode("91310000MA1");
        tenant.setAddress("上海市");
        tenant.setDomain("demo.artemis.com");
        tenant.setIntro("示例租户");
        tenant.setPackageId(packageId);
        tenant.setExpireTime(LocalDateTime.parse("2030-01-01T00:00:00"));
        tenant.setUserLimit(100);
        tenant.setStatus("NORMAL");
        tenant.setRemarks("基础说明");
        return tenant;
    }
}
