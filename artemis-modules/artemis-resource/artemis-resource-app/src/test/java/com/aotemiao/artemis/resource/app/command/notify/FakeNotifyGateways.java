package com.aotemiao.artemis.resource.app.command.notify;

import com.aotemiao.artemis.resource.domain.gateway.notify.EmailProviderGateway;
import com.aotemiao.artemis.resource.domain.gateway.notify.SmsBlacklistGateway;
import com.aotemiao.artemis.resource.domain.gateway.notify.SmsProviderGateway;
import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

final class FakeNotifyGateways implements SmsProviderGateway, EmailProviderGateway, SmsBlacklistGateway {

    private final Set<String> blacklistedPhones = new HashSet<>();

    @Override
    public DeliveryResult sendText(String phone, String content, String provider, String extJson) {
        return new DeliveryResult("sms-1", phone, provider, "ACCEPTED");
    }

    @Override
    public DeliveryResult sendTemplate(
            String phone, String templateCode, String templateParams, String provider, String extJson) {
        return new DeliveryResult("sms-template-1", phone, provider, "ACCEPTED");
    }

    @Override
    public DeliveryResult sendAsync(String phone, String content, String provider, String extJson) {
        return new DeliveryResult("sms-async-1", phone, provider, "ACCEPTED");
    }

    @Override
    public DeliveryResult sendDelayed(
            String phone, String content, LocalDateTime delayedAt, String provider, String extJson) {
        return new DeliveryResult("sms-delayed-1", phone, provider, "ACCEPTED");
    }

    @Override
    public DeliveryResult sendEmail(String to, String subject, String content, String provider, String extJson) {
        return new DeliveryResult("mail-1", to, provider, "ACCEPTED");
    }

    @Override
    public boolean contains(String phone) {
        return blacklistedPhones.contains(phone);
    }

    @Override
    public void add(String phone) {
        blacklistedPhones.add(phone);
    }

    @Override
    public void remove(String phone) {
        blacklistedPhones.remove(phone);
    }
}
