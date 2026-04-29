package com.aotemiao.artemis.resource.app.command.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import org.junit.jupiter.api.Test;

class MarkSystemMessageReadCmdExeTest {

    @Test
    void execute_marksMessageRead() {
        FakeSystemMessageGateway gateway = new FakeSystemMessageGateway();
        SystemMessage message = new SystemMessage();
        message.setTitle("Hi");
        message.setContent("Welcome");
        message.setSender("system");
        message.setRecipientUserId(1L);
        message.setBroadcastFlag(0);
        message.setReadFlag(0);
        gateway.save(message);

        SystemMessage read = new MarkSystemMessageReadCmdExe(gateway).execute(new MarkSystemMessageReadCmd(1L, 1L));

        assertThat(read.getReadFlag()).isEqualTo(1);
        assertThat(read.getReadTime()).isNotNull();
    }

    @Test
    void execute_rejectsOtherUserMessage() {
        FakeSystemMessageGateway gateway = new FakeSystemMessageGateway();
        SystemMessage message = new SystemMessage();
        message.setRecipientUserId(1L);
        message.setReadFlag(0);
        gateway.save(message);

        assertThatThrownBy(() -> new MarkSystemMessageReadCmdExe(gateway).execute(new MarkSystemMessageReadCmd(1L, 2L)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("belong");
    }
}
