package com.aotemiao.artemis.resource.adapter.dubbo.notify;

import com.aotemiao.artemis.resource.app.command.notify.ManageSmsBlacklistCmd;
import com.aotemiao.artemis.resource.app.command.notify.ManageSmsBlacklistCmdExe;
import com.aotemiao.artemis.resource.app.command.notify.SendSmsCmd;
import com.aotemiao.artemis.resource.app.command.notify.SendSmsCmdExe;
import com.aotemiao.artemis.resource.client.api.notify.ResourceSmsService;
import com.aotemiao.artemis.resource.client.dto.notify.SmsBatchSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsDelayedSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsDeliveryResponse;
import com.aotemiao.artemis.resource.client.dto.notify.SmsSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsTemplateSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsVerificationCodeRequest;
import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class ResourceSmsServiceDubboImpl implements ResourceSmsService {

    private final SendSmsCmdExe sendSmsCmdExe;

    private final ManageSmsBlacklistCmdExe manageSmsBlacklistCmdExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dubbo adapter keeps Spring executors and does not expose them.")
    public ResourceSmsServiceDubboImpl(SendSmsCmdExe sendSmsCmdExe, ManageSmsBlacklistCmdExe manageSmsBlacklistCmdExe) {
        this.sendSmsCmdExe = sendSmsCmdExe;
        this.manageSmsBlacklistCmdExe = manageSmsBlacklistCmdExe;
    }

    @Override
    public SmsDeliveryResponse sendVerificationCode(SmsVerificationCodeRequest request) {
        return toResponse(sendSmsCmdExe.sendVerificationCode(new SendSmsCmd(
                request.phone(),
                List.of(),
                null,
                null,
                null,
                request.scene(),
                null,
                request.provider(),
                request.extJson())));
    }

    @Override
    public SmsDeliveryResponse sendSingle(SmsSendRequest request) {
        return toResponse(sendSmsCmdExe.sendSingle(new SendSmsCmd(
                request.phone(),
                List.of(),
                request.content(),
                null,
                null,
                null,
                null,
                request.provider(),
                request.extJson())));
    }

    @Override
    public List<SmsDeliveryResponse> sendBatch(SmsBatchSendRequest request) {
        return sendSmsCmdExe
                .sendBatch(new SendSmsCmd(
                        null,
                        request.phones(),
                        request.content(),
                        null,
                        null,
                        null,
                        null,
                        request.provider(),
                        request.extJson()))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SmsDeliveryResponse sendTemplate(SmsTemplateSendRequest request) {
        return toResponse(sendSmsCmdExe.sendTemplate(new SendSmsCmd(
                request.phone(),
                List.of(),
                null,
                request.templateCode(),
                request.templateParams(),
                null,
                null,
                request.provider(),
                request.extJson())));
    }

    @Override
    public SmsDeliveryResponse sendAsync(SmsSendRequest request) {
        return toResponse(sendSmsCmdExe.sendAsync(new SendSmsCmd(
                request.phone(),
                List.of(),
                request.content(),
                null,
                null,
                null,
                null,
                request.provider(),
                request.extJson())));
    }

    @Override
    public SmsDeliveryResponse sendDelayed(SmsDelayedSendRequest request) {
        return toResponse(sendSmsCmdExe.sendDelayed(new SendSmsCmd(
                request.phone(),
                List.of(),
                request.content(),
                null,
                null,
                null,
                request.delayedAt(),
                request.provider(),
                request.extJson())));
    }

    @Override
    public void addBlacklist(String phone) {
        manageSmsBlacklistCmdExe.add(new ManageSmsBlacklistCmd(phone));
    }

    @Override
    public void removeBlacklist(String phone) {
        manageSmsBlacklistCmdExe.remove(new ManageSmsBlacklistCmd(phone));
    }

    private SmsDeliveryResponse toResponse(DeliveryResult result) {
        return new SmsDeliveryResponse(result.messageId(), result.target(), result.provider(), result.status());
    }
}
