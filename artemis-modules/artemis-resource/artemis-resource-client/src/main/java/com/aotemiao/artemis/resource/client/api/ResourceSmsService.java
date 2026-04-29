package com.aotemiao.artemis.resource.client.api;

import com.aotemiao.artemis.resource.client.dto.SmsBatchSendRequest;
import com.aotemiao.artemis.resource.client.dto.SmsDelayedSendRequest;
import com.aotemiao.artemis.resource.client.dto.SmsDeliveryResponse;
import com.aotemiao.artemis.resource.client.dto.SmsSendRequest;
import com.aotemiao.artemis.resource.client.dto.SmsTemplateSendRequest;
import com.aotemiao.artemis.resource.client.dto.SmsVerificationCodeRequest;
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
