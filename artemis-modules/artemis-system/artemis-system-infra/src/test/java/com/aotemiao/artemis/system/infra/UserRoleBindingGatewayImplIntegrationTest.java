package com.aotemiao.artemis.system.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.gateway.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.SystemRoleGatewayImpl.class,
    com.aotemiao.artemis.system.infra.gateway.UserRoleBindingGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:user_role_binding_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class UserRoleBindingGatewayImplIntegrationTest {

    @Autowired
    private SystemRoleGateway systemRoleGateway;

    @Autowired
    private UserRoleBindingGateway userRoleBindingGateway;

    @Test
    void replaceRoles_thenFindRolesByUserId_returnsCurrentBindings() {
        SystemRole admin = new SystemRole();
        admin.setRoleKey("super-admin");
        admin.setRoleName("超级管理员");
        admin.setEnabled(true);
        admin = systemRoleGateway.save(admin);

        SystemRole auditor = new SystemRole();
        auditor.setRoleKey("auditor");
        auditor.setRoleName("审计员");
        auditor.setEnabled(true);
        auditor = systemRoleGateway.save(auditor);

        userRoleBindingGateway.replaceRoles(7L, List.of(admin.getId(), auditor.getId(), auditor.getId()));

        List<SystemRole> firstBinding = userRoleBindingGateway.findRolesByUserId(7L);
        assertThat(firstBinding).hasSize(2);
        assertThat(firstBinding.stream().map(SystemRole::getRoleKey))
                .containsExactlyInAnyOrder("super-admin", "auditor");

        userRoleBindingGateway.replaceRoles(7L, List.of(admin.getId()));

        List<SystemRole> secondBinding = userRoleBindingGateway.findRolesByUserId(7L);
        assertThat(secondBinding).hasSize(1);
        assertThat(secondBinding.getFirst().getRoleKey()).isEqualTo("super-admin");
    }
}
