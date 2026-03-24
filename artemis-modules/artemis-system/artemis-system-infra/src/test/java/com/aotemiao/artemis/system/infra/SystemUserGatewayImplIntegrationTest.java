package com.aotemiao.artemis.system.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import com.aotemiao.artemis.system.infra.repository.SystemUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@EnableJdbcRepositories(basePackageClasses = SystemUserRepository.class)
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.SystemUserGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_user_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql"
        })
class SystemUserGatewayImplIntegrationTest {

    @Autowired
    private SystemUserGateway systemUserGateway;

    @Test
    void save_thenFindById_returnsSameData() {
        SystemUser systemUser = new SystemUser();
        systemUser.setUsername("alice");
        systemUser.setDisplayName("Alice");
        systemUser.setPassword("secret");
        systemUser.setEnabled(true);

        SystemUser saved = systemUserGateway.save(systemUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("alice");

        Optional<SystemUser> found = systemUserGateway.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Alice");
        assertThat(found.get().isEnabled()).isTrue();
    }

    @Test
    void findByUsername_returnsSavedUser() {
        SystemUser systemUser = new SystemUser();
        systemUser.setUsername("bob");
        systemUser.setDisplayName("Bob");
        systemUser.setPassword("secret");
        systemUser.setEnabled(true);
        systemUserGateway.save(systemUser);

        Optional<SystemUser> found = systemUserGateway.findByUsername("bob");

        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Bob");
    }

    @Test
    void findPage_returnsSavedUsers() {
        SystemUser alice = new SystemUser();
        alice.setUsername("alice");
        alice.setDisplayName("Alice");
        alice.setPassword("secret");
        alice.setEnabled(true);
        systemUserGateway.save(alice);

        SystemUser bob = new SystemUser();
        bob.setUsername("bob");
        bob.setDisplayName("Bob");
        bob.setPassword("secret");
        bob.setEnabled(false);
        systemUserGateway.save(bob);

        PageResult<SystemUser> pageResult = systemUserGateway.findPage(new PageRequest(0, 10));

        assertThat(pageResult.content()).hasSize(2);
        assertThat(pageResult.total()).isEqualTo(2);
        assertThat(pageResult.content().stream().map(SystemUser::getUsername))
                .containsExactlyInAnyOrder("alice", "bob");
    }
}
