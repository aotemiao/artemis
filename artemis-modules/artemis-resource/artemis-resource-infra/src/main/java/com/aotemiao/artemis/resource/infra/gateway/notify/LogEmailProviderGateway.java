package com.aotemiao.artemis.resource.infra.gateway.notify;

import com.aotemiao.artemis.resource.domain.gateway.notify.EmailProviderGateway;
import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LogEmailProviderGateway implements EmailProviderGateway {

    @Override
    public DeliveryResult sendEmail(String to, String subject, String content, String provider, String extJson) {
        return new DeliveryResult(UUID.randomUUID().toString(), to, provider, "ACCEPTED");
    }
}
