package com.aotemiao.artemis.resource.adapter.web.message;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.resource.adapter.web.dto.message.PublishSystemMessageRequest;
import com.aotemiao.artemis.resource.adapter.web.dto.message.SystemMessageDTO;
import com.aotemiao.artemis.resource.app.command.message.MarkSystemMessageReadCmd;
import com.aotemiao.artemis.resource.app.command.message.MarkSystemMessageReadCmdExe;
import com.aotemiao.artemis.resource.app.command.message.PublishSystemMessageCmd;
import com.aotemiao.artemis.resource.app.command.message.PublishSystemMessageCmdExe;
import com.aotemiao.artemis.resource.app.query.message.SystemMessageInboxQry;
import com.aotemiao.artemis.resource.app.query.message.SystemMessageInboxQryExe;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 站内消息 REST API。 */
@RestController
@RequestMapping(SystemMessageController.BASE_PATH)
public class SystemMessageController {

    public static final String BASE_PATH = "/api/resource/messages";

    private final PublishSystemMessageCmdExe publishSystemMessageCmdExe;
    private final MarkSystemMessageReadCmdExe markSystemMessageReadCmdExe;
    private final SystemMessageInboxQryExe systemMessageInboxQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this controller does not expose them.")
    public SystemMessageController(
            PublishSystemMessageCmdExe publishSystemMessageCmdExe,
            MarkSystemMessageReadCmdExe markSystemMessageReadCmdExe,
            SystemMessageInboxQryExe systemMessageInboxQryExe) {
        this.publishSystemMessageCmdExe = publishSystemMessageCmdExe;
        this.markSystemMessageReadCmdExe = markSystemMessageReadCmdExe;
        this.systemMessageInboxQryExe = systemMessageInboxQryExe;
    }

    @PostMapping("/user")
    public R<SystemMessageDTO> publishToUser(@RequestBody PublishSystemMessageRequest request) {
        List<SystemMessage> messages = publishSystemMessageCmdExe.execute(toCmd(request));
        return R.ok(toDTO(messages.getFirst()));
    }

    @PostMapping("/broadcast")
    public R<List<SystemMessageDTO>> publishToAll(@RequestBody PublishSystemMessageRequest request) {
        return R.ok(publishSystemMessageCmdExe.execute(toCmd(request)).stream()
                .map(this::toDTO)
                .toList());
    }

    @GetMapping("/inbox")
    public R<PageResult<SystemMessageDTO>> inbox(
            @RequestParam Long recipientUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<SystemMessage> pageResult = systemMessageInboxQryExe.execute(
                new SystemMessageInboxQry(recipientUserId, new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(this::toDTO).toList(),
                pageResult.totalPages()));
    }

    @PutMapping("/{id}/read")
    public R<SystemMessageDTO> markRead(@PathVariable Long id, @RequestParam Long recipientUserId) {
        return R.ok(toDTO(markSystemMessageReadCmdExe.execute(new MarkSystemMessageReadCmd(id, recipientUserId))));
    }

    private PublishSystemMessageCmd toCmd(PublishSystemMessageRequest request) {
        return new PublishSystemMessageCmd(
                request.title(),
                request.content(),
                request.sender(),
                request.recipientUserId(),
                request.recipientUserIds(),
                request.extJson());
    }

    private SystemMessageDTO toDTO(SystemMessage message) {
        return new SystemMessageDTO(
                message.getId(),
                message.getTitle(),
                message.getContent(),
                message.getSender(),
                message.getRecipientUserId(),
                message.getBroadcastFlag(),
                message.getReadFlag(),
                message.getReadTime(),
                message.getExtJson());
    }
}
