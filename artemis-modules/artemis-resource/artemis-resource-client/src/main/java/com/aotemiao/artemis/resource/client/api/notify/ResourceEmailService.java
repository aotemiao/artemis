package com.aotemiao.artemis.resource.client.api.notify;

import com.aotemiao.artemis.resource.client.dto.notify.EmailDeliveryResponse;
import com.aotemiao.artemis.resource.client.dto.notify.EmailSendRequest;

/** 资源服务邮件远程接口。 */
public interface ResourceEmailService {

    EmailDeliveryResponse sendEmail(EmailSendRequest request);
}
