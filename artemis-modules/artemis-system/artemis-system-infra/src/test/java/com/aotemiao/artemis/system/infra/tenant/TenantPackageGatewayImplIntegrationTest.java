package com.aotemiao.artemis.system.infra.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
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
    com.aotemiao.artemis.system.infra.gateway.tenant.TenantPackageGatewayImpl.class
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

    private static TenantPackage samplePackage(String packageName, boolean enabled, List<Long> menuIds) {
        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.setPackageName(packageName);
        tenantPackage.setMenuCheckStrictly(true);
        tenantPackage.setEnabled(enabled);
        tenantPackage.setRemarks("测试套餐");
        tenantPackage.setMenuIds(menuIds);
        return tenantPackage;
    }
}
