package com.aotemiao.artemis.resource.app.command.notify;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.notify.SmsBlacklistGateway;
import com.aotemiao.artemis.resource.domain.gateway.notify.SmsProviderGateway;
import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SendSmsCmdExe {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SmsProviderGateway smsProviderGateway;

    private final SmsBlacklistGateway smsBlacklistGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    public SendSmsCmdExe(SmsProviderGateway smsProviderGateway, SmsBlacklistGateway smsBlacklistGateway) {
        this.smsProviderGateway = smsProviderGateway;
        this.smsBlacklistGateway = smsBlacklistGateway;
    }

    public DeliveryResult sendVerificationCode(SendSmsCmd cmd) {
        requirePhone(cmd.phone());
        return sendText(cmd.phone(), "验证码：" + verificationCode(), cmd.provider(), cmd.extJson());
    }

    public DeliveryResult sendSingle(SendSmsCmd cmd) {
        requireText(cmd.content(), "Sms content must not be blank");
        return sendText(cmd.phone(), cmd.content(), cmd.provider(), cmd.extJson());
    }

    public List<DeliveryResult> sendBatch(SendSmsCmd cmd) {
        if (cmd.phones().isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Sms phones must not be empty");
        }
        List<DeliveryResult> results = new ArrayList<>();
        for (String phone : cmd.phones().stream().distinct().toList()) {
            results.add(sendText(phone, cmd.content(), cmd.provider(), cmd.extJson()));
        }
        return results;
    }

    public DeliveryResult sendTemplate(SendSmsCmd cmd) {
        requirePhone(cmd.phone());
        requireText(cmd.templateCode(), "Sms template code must not be blank");
        assertNotBlacklisted(cmd.phone());
        return smsProviderGateway.sendTemplate(
                cmd.phone(), cmd.templateCode().strip(), cmd.templateParams(), provider(cmd.provider()), cmd.extJson());
    }

    public DeliveryResult sendAsync(SendSmsCmd cmd) {
        requireText(cmd.content(), "Sms content must not be blank");
        requirePhone(cmd.phone());
        assertNotBlacklisted(cmd.phone());
        return smsProviderGateway.sendAsync(
                cmd.phone(), cmd.content().strip(), provider(cmd.provider()), cmd.extJson());
    }

    public DeliveryResult sendDelayed(SendSmsCmd cmd) {
        requireText(cmd.content(), "Sms content must not be blank");
        requirePhone(cmd.phone());
        if (cmd.delayedAt() == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Sms delayed time must not be null");
        }
        assertNotBlacklisted(cmd.phone());
        return smsProviderGateway.sendDelayed(
                cmd.phone(), cmd.content().strip(), cmd.delayedAt(), provider(cmd.provider()), cmd.extJson());
    }

    private DeliveryResult sendText(String phone, String content, String provider, String extJson) {
        requirePhone(phone);
        requireText(content, "Sms content must not be blank");
        assertNotBlacklisted(phone);
        return smsProviderGateway.sendText(phone, content.strip(), provider(provider), extJson);
    }

    private void assertNotBlacklisted(String phone) {
        if (smsBlacklistGateway.contains(phone)) {
            throw new BizException(CommonErrorCode.FORBIDDEN, "Sms phone is blacklisted: " + phone);
        }
    }

    private String verificationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String provider(String provider) {
        return provider == null || provider.isBlank() ? "LOG" : provider.strip().toUpperCase();
    }

    private void requirePhone(String phone) {
        requireText(phone, "Sms phone must not be blank");
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, message);
        }
    }
}
