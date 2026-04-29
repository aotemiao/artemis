package com.aotemiao.artemis.resource.infra.gateway.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.resource.domain.gateway.message.SystemMessageGateway;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.resource.infra.gateway.message.SystemMessageGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:resource_system_message_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.resource.infra.repository"
        })
class SystemMessageGatewayImplIntegrationTest {

    @Autowired
    private SystemMessageGateway systemMessageGateway;

    @Test
    void save_thenFindInbox_returnsMessage() {
        SystemMessage saved = systemMessageGateway.save(sampleMessage(1L));
        systemMessageGateway.save(sampleMessage(2L));

        assertThat(systemMessageGateway.findById(saved.getId())).isPresent();
        assertThat(systemMessageGateway.findInbox(1L, new PageRequest(0, 10)).content())
                .extracting(SystemMessage::getTitle)
                .containsExactly("Hi");
    }

    private static SystemMessage sampleMessage(Long recipientUserId) {
        SystemMessage message = new SystemMessage();
        message.setTitle("Hi");
        message.setContent("Welcome");
        message.setSender("system");
        message.setRecipientUserId(recipientUserId);
        message.setBroadcastFlag(0);
        message.setReadFlag(0);
        message.setExtJson("{}");
        return message;
    }
}
