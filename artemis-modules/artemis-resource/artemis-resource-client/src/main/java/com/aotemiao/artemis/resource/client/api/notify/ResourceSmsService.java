package com.aotemiao.artemis.resource.client.api.notify;

import com.aotemiao.artemis.resource.client.dto.notify.SmsBatchSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsDelayedSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsDeliveryResponse;
import com.aotemiao.artemis.resource.client.dto.notify.SmsSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsTemplateSendRequest;
import com.aotemiao.artemis.resource.client.dto.notify.SmsVerificationCodeRequest;
import java.util.List;

/** 资源服务短信远程接口。 */
public interface ResourceSmsService {

    SmsDeliveryResponse sendVerificationCode(SmsVerificationCodeRequest request);

    SmsDeliveryResponse sendSingle(SmsSendRequest request);

    List<SmsDeliveryResponse> sendBatch(SmsBatchSendRequest request);

    SmsDeliveryResponse sendTemplate(SmsTemplateSendRequest request);

    SmsDeliveryResponse sendAsync(SmsSendRequest request);

    SmsDeliveryResponse sendDelayed(SmsDelayedSendRequest request);

    void addBlacklist(String phone);

    void removeBlacklist(String phone);
}
