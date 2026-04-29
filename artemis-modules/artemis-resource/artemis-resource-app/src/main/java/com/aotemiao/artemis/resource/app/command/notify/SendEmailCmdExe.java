package com.aotemiao.artemis.resource.app.command.notify;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.notify.EmailProviderGateway;
import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

@Service
public class SendEmailCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final EmailProviderGateway emailProviderGateway;

    public SendEmailCmdExe(EmailProviderGateway emailProviderGateway) {
        this.emailProviderGateway = emailProviderGateway;
    }

    public DeliveryResult execute(SendEmailCmd cmd) {
        requireText(cmd.to(), "Email recipient must not be blank");
        requireText(cmd.subject(), "Email subject must not be blank");
        requireText(cmd.content(), "Email content must not be blank");
        return emailProviderGateway.sendEmail(
                cmd.to().strip(),
                cmd.subject().strip(),
                cmd.content().strip(),
                provider(cmd.provider()),
                cmd.extJson());
    }

    private String provider(String provider) {
        return provider == null || provider.isBlank() ? "LOG" : provider.strip().toUpperCase();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, message);
        }
    }
}
