package com.aotemiao.artemis.resource.app.command.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import org.junit.jupiter.api.Test;

class SendEmailCmdExeTest {

    @Test
    void execute_delegatesToProvider() {
        var result = new SendEmailCmdExe(new FakeNotifyGateways())
                .execute(new SendEmailCmd("dev@example.com", "Hi", "Welcome", null, null));

        assertThat(result.messageId()).isEqualTo("mail-1");
        assertThat(result.provider()).isEqualTo("LOG");
    }

    @Test
    void execute_rejectsBlankRecipient() {
        assertThatThrownBy(() -> new SendEmailCmdExe(new FakeNotifyGateways())
                        .execute(new SendEmailCmd(" ", "Hi", "Welcome", null, null)))
                .isInstanceOf(BizException.class);
    }
}
