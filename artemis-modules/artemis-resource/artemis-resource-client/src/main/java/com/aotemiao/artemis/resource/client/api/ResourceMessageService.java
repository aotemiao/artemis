package com.aotemiao.artemis.resource.client.api;

import com.aotemiao.artemis.resource.client.dto.PublishMessageRequest;
import com.aotemiao.artemis.resource.client.dto.PublishedMessageResponse;
import java.util.List;

/** 资源服务站内消息远程接口。 */
public interface ResourceMessageService {

    PublishedMessageResponse publishToUser(PublishMessageRequest request);

    List<PublishedMessageResponse> publishToAll(PublishMessageRequest request);
}
