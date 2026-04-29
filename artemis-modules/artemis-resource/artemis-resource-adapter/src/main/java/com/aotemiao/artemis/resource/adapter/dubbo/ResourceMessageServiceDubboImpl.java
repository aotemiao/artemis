package com.aotemiao.artemis.resource.adapter.dubbo;

import com.aotemiao.artemis.resource.app.command.message.PublishSystemMessageCmd;
import com.aotemiao.artemis.resource.app.command.message.PublishSystemMessageCmdExe;
import com.aotemiao.artemis.resource.client.api.ResourceMessageService;
import com.aotemiao.artemis.resource.client.dto.PublishMessageRequest;
import com.aotemiao.artemis.resource.client.dto.PublishedMessageResponse;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class ResourceMessageServiceDubboImpl implements ResourceMessageService {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Dubbo adapter keeps Spring executor collaborator and does not expose it.")
    private final PublishSystemMessageCmdExe publishSystemMessageCmdExe;

    public ResourceMessageServiceDubboImpl(PublishSystemMessageCmdExe publishSystemMessageCmdExe) {
        this.publishSystemMessageCmdExe = publishSystemMessageCmdExe;
    }

    @Override
    public PublishedMessageResponse publishToUser(PublishMessageRequest request) {
        return toResponse(publishSystemMessageCmdExe.execute(toCmd(request)).getFirst());
    }

    @Override
    public List<PublishedMessageResponse> publishToAll(PublishMessageRequest request) {
        return publishSystemMessageCmdExe.execute(toCmd(request)).stream()
                .map(this::toResponse)
                .toList();
    }

    private PublishSystemMessageCmd toCmd(PublishMessageRequest request) {
        return new PublishSystemMessageCmd(
                request.title(),
                request.content(),
                request.sender(),
                request.recipientUserId(),
                request.recipientUserIds(),
                request.extJson());
    }

    private PublishedMessageResponse toResponse(SystemMessage message) {
        return new PublishedMessageResponse(
                message.getId(), message.getRecipientUserId(), message.getTitle(), message.getReadFlag());
    }
}
