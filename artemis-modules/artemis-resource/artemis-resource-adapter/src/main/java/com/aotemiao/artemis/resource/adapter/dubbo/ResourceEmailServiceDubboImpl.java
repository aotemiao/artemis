package com.aotemiao.artemis.resource.adapter.dubbo;

import com.aotemiao.artemis.resource.app.command.notify.SendEmailCmd;
import com.aotemiao.artemis.resource.app.command.notify.SendEmailCmdExe;
import com.aotemiao.artemis.resource.client.api.ResourceEmailService;
import com.aotemiao.artemis.resource.client.dto.EmailDeliveryResponse;
import com.aotemiao.artemis.resource.client.dto.EmailSendRequest;
import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class ResourceEmailServiceDubboImpl implements ResourceEmailService {

    private final SendEmailCmdExe sendEmailCmdExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dubbo adapter keeps Spring executor and does not expose it.")
    public ResourceEmailServiceDubboImpl(SendEmailCmdExe sendEmailCmdExe) {
        this.sendEmailCmdExe = sendEmailCmdExe;
    }

    @Override
    public EmailDeliveryResponse sendEmail(EmailSendRequest request) {
        DeliveryResult result = sendEmailCmdExe.execute(new SendEmailCmd(
                request.to(), request.subject(), request.content(), request.provider(), request.extJson()));
        return new EmailDeliveryResponse(result.messageId(), result.target(), result.provider(), result.status());
    }
}
