package com.aotemiao.artemis.system.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.system.infra.gateway.UserCredentialsGatewayImpl;
import org.junit.jupiter.api.Test;

class UserCredentialsGatewayImplTest {

    private final UserCredentialsGatewayImpl userCredentialsGateway = new UserCredentialsGatewayImpl();

    @Test
    void validate_whenStubCredentialsMatch_returnsUserId() {
        assertThat(userCredentialsGateway.validate("admin", "123456")).contains(1L);
    }

    @Test
    void validate_whenCredentialsDoNotMatch_returnsEmpty() {
        assertThat(userCredentialsGateway.validate("admin", "wrong")).isEmpty();
        assertThat(userCredentialsGateway.validate(null, "123456")).isEmpty();
    }
}
