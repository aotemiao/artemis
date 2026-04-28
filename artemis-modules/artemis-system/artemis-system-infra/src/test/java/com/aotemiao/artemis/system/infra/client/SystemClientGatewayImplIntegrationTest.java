package com.aotemiao.artemis.system.infra.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.client.SystemClientGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_client_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class SystemClientGatewayImplIntegrationTest {

    @Autowired
    private SystemClientGateway systemClientGateway;

    @Test
    void save_thenFindByIdAndClientId_returnsSameData() {
        SystemClient saved = systemClientGateway.save(sampleClient("artemis-admin", "artemis-admin-web"));

        Optional<SystemClient> foundById = systemClientGateway.findById(saved.getId());
        Optional<SystemClient> foundByClientId = systemClientGateway.findByClientId("artemis-admin");

        assertThat(foundById).isPresent();
        assertThat(foundById.get().supportsGrantType("password")).isTrue();
        assertThat(foundByClientId).isPresent();
    }

    @Test
    void findPage_returnsSavedClients() {
        systemClientGateway.save(sampleClient("artemis-admin", "artemis-admin-web"));

        assertThat(systemClientGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(SystemClient::getClientId)
                .containsExactly("artemis-admin");
    }

    @Test
    void deleteById_hidesClientFromQueries() {
        SystemClient saved = systemClientGateway.save(sampleClient("artemis-admin", "artemis-admin-web"));

        systemClientGateway.deleteById(saved.getId());

        assertThat(systemClientGateway.findById(saved.getId())).isEmpty();
        assertThat(systemClientGateway.findByClientId("artemis-admin")).isEmpty();
    }

    private static SystemClient sampleClient(String clientId, String clientKey) {
        SystemClient client = new SystemClient();
        client.setClientId(clientId);
        client.setClientKey(clientKey);
        client.setClientSecret("change-me");
        client.setGrantTypes("password,refresh_token");
        client.setDeviceType("PC");
        client.setActiveTimeoutSeconds(1800L);
        client.setFixedTimeoutSeconds(86400L);
        client.setStatus("NORMAL");
        return client;
    }
}
