package com.aotemiao.artemis.resource.infra.gateway.notify;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotifyProviderGatewayTest {

    @Test
    void logProviders_acceptMessages() {
        assertThat(new LogSmsProviderGateway()
                        .sendText("1", "hello", "LOG", null)
                        .status())
                .isEqualTo("ACCEPTED");
        assertThat(new LogEmailProviderGateway()
                        .sendEmail("dev@example.com", "Hi", "Welcome", "LOG", null)
                        .status())
                .isEqualTo("ACCEPTED");
    }

    @Test
    void blacklist_addsAndRemovesPhone() {
        InMemorySmsBlacklistGateway gateway = new InMemorySmsBlacklistGateway();

        gateway.add("1");
        assertThat(gateway.contains("1")).isTrue();
        gateway.remove("1");
        assertThat(gateway.contains("1")).isFalse();
    }
}
