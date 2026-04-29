package com.aotemiao.artemis.resource.client.api;

import com.aotemiao.artemis.resource.client.dto.EmailDeliveryResponse;
import com.aotemiao.artemis.resource.client.dto.EmailSendRequest;

/** 资源服务邮件远程接口。 */
public interface ResourceEmailService {

    EmailDeliveryResponse sendEmail(EmailSendRequest request);
}
