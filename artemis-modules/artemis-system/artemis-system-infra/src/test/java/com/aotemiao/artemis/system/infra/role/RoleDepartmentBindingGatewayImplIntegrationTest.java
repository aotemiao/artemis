package com.aotemiao.artemis.system.infra.role;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.system.domain.gateway.role.RoleDepartmentBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.role.SystemRoleGatewayImpl.class,
    com.aotemiao.artemis.system.infra.gateway.role.RoleDepartmentBindingGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:role_department_binding_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class RoleDepartmentBindingGatewayImplIntegrationTest {

    @Autowired
    private SystemRoleGateway systemRoleGateway;

    @Autowired
    private RoleDepartmentBindingGateway roleDepartmentBindingGateway;

    @Test
    void replaceDepartments_thenFindDepartmentIdsByRoleId_returnsCurrentBindings() {
        SystemRole role = saveRole("auditor", "审计员");

        roleDepartmentBindingGateway.replaceDepartments(role.getId(), List.of(10L, 20L, 20L));

        assertThat(roleDepartmentBindingGateway.findDepartmentIdsByRoleId(role.getId()))
                .containsExactly(10L, 20L);

        roleDepartmentBindingGateway.replaceDepartments(role.getId(), List.of(30L));

        assertThat(roleDepartmentBindingGateway.findDepartmentIdsByRoleId(role.getId()))
                .containsExactly(30L);
    }

    private SystemRole saveRole(String roleKey, String roleName) {
        SystemRole role = new SystemRole();
        role.setRoleKey(roleKey);
        role.setRoleName(roleName);
        role.setDataScope("CUSTOM");
        role.setEnabled(true);
        return systemRoleGateway.save(role);
    }
}
