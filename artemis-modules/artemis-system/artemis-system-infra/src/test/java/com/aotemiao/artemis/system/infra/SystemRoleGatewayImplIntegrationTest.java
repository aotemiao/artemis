package com.aotemiao.artemis.system.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.SystemRoleGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_role_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class SystemRoleGatewayImplIntegrationTest {

    @Autowired
    private SystemRoleGateway systemRoleGateway;

    @Test
    void save_thenFindById_returnsSameData() {
        SystemRole systemRole = new SystemRole();
        systemRole.setRoleKey("super-admin");
        systemRole.setRoleName("超级管理员");
        systemRole.setEnabled(true);

        SystemRole saved = systemRoleGateway.save(systemRole);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRoleKey()).isEqualTo("super-admin");

        Optional<SystemRole> found = systemRoleGateway.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRoleName()).isEqualTo("超级管理员");
        assertThat(found.get().isEnabled()).isTrue();
    }

    @Test
    void findPage_returnsSavedRoles() {
        SystemRole admin = new SystemRole();
        admin.setRoleKey("super-admin");
        admin.setRoleName("超级管理员");
        admin.setEnabled(true);
        systemRoleGateway.save(admin);

        SystemRole auditor = new SystemRole();
        auditor.setRoleKey("auditor");
        auditor.setRoleName("审计员");
        auditor.setEnabled(false);
        systemRoleGateway.save(auditor);

        PageResult<SystemRole> pageResult = systemRoleGateway.findPage(new PageRequest(0, 10));

        assertThat(pageResult.content()).hasSize(2);
        assertThat(pageResult.total()).isEqualTo(2);
        assertThat(pageResult.content().stream().map(SystemRole::getRoleKey))
                .containsExactlyInAnyOrder("super-admin", "auditor");
    }
}
