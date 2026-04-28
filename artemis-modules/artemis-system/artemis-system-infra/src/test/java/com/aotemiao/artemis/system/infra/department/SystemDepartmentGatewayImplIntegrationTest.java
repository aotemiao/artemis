package com.aotemiao.artemis.system.infra.department;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
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
    com.aotemiao.artemis.system.infra.gateway.department.SystemDepartmentGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_department_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class SystemDepartmentGatewayImplIntegrationTest {

    @Autowired
    private SystemDepartmentGateway systemDepartmentGateway;

    @Test
    void save_thenFindByIdAndName_returnsSameData() {
        SystemDepartment saved = systemDepartmentGateway.save(sampleDepartment(0L, "0", "总部"));

        Optional<SystemDepartment> foundById = systemDepartmentGateway.findById(saved.getId());
        Optional<SystemDepartment> foundByName = systemDepartmentGateway.findByParentIdAndDeptName(0L, "总部");

        assertThat(foundById).isPresent();
        assertThat(foundById.get().getDeptName()).isEqualTo("总部");
        assertThat(foundByName).isPresent();
    }

    @Test
    void findAll_returnsDepartmentsInSortOrder() {
        systemDepartmentGateway.save(sampleDepartment(0L, "0", "总部"));
        systemDepartmentGateway.save(sampleDepartment(1L, "0,1", "研发部"));

        List<SystemDepartment> departments = systemDepartmentGateway.findAll();

        assertThat(departments).extracting(SystemDepartment::getDeptName).contains("总部", "研发部");
    }

    @Test
    void deleteById_hidesDepartmentFromQueries() {
        SystemDepartment saved = systemDepartmentGateway.save(sampleDepartment(0L, "0", "总部"));

        systemDepartmentGateway.deleteById(saved.getId());

        assertThat(systemDepartmentGateway.findById(saved.getId())).isEmpty();
        assertThat(systemDepartmentGateway.findByParentIdAndDeptName(0L, "总部")).isEmpty();
    }

    private static SystemDepartment sampleDepartment(Long parentId, String ancestors, String deptName) {
        SystemDepartment department = new SystemDepartment();
        department.setParentId(parentId);
        department.setAncestors(ancestors);
        department.setDeptName(deptName);
        department.setDeptCategory("DEPT");
        department.setSortOrder(10);
        department.setStatus("NORMAL");
        return department;
    }
}
