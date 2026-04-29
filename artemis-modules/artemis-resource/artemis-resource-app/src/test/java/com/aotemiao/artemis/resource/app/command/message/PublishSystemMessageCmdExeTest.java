package com.aotemiao.artemis.resource.app.command.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import java.util.List;
import org.junit.jupiter.api.Test;

class PublishSystemMessageCmdExeTest {

    @Test
    void execute_publishesDeduplicatedMessages() {
        PublishSystemMessageCmdExe exe = new PublishSystemMessageCmdExe(new FakeSystemMessageGateway());

        List<SystemMessage> messages =
                exe.execute(new PublishSystemMessageCmd("Hi", "Welcome", "system", 1L, List.of(1L, 2L), "{}"));

        assertThat(messages).hasSize(2);
        assertThat(messages).extracting(SystemMessage::getRecipientUserId).containsExactly(1L, 2L);
        assertThat(messages).extracting(SystemMessage::getBroadcastFlag).containsOnly(1);
    }

    @Test
    void execute_rejectsEmptyRecipients() {
        PublishSystemMessageCmdExe exe = new PublishSystemMessageCmdExe(new FakeSystemMessageGateway());

        assertThatThrownBy(() -> exe.execute(new PublishSystemMessageCmd("Hi", "Welcome", null, null, List.of(), null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Recipient");
    }
}
