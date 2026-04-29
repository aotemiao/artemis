package com.aotemiao.artemis.resource.app.command.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SendSmsCmdExeTest {

    @Test
    void sendSmsVariants_delegateToProvider() {
        FakeNotifyGateways gateways = new FakeNotifyGateways();
        SendSmsCmdExe exe = new SendSmsCmdExe(gateways, gateways);

        assertThat(exe.sendVerificationCode(cmd("13800000000")).status()).isEqualTo("ACCEPTED");
        assertThat(exe.sendSingle(cmd("13800000000")).provider()).isEqualTo("LOG");
        assertThat(exe.sendBatch(
                        new SendSmsCmd(null, List.of("1", "1", "2"), "hello", null, null, null, null, null, null)))
                .hasSize(2);
        assertThat(exe.sendTemplate(new SendSmsCmd("1", List.of(), null, "TPL", "{}", null, null, "aliyun", null))
                        .provider())
                .isEqualTo("ALIYUN");
        assertThat(exe.sendAsync(cmd("1")).messageId()).isEqualTo("sms-async-1");
        assertThat(exe.sendDelayed(new SendSmsCmd(
                                "1", List.of(), "hello", null, null, null, LocalDateTime.now(), null, null))
                        .messageId())
                .isEqualTo("sms-delayed-1");
    }

    @Test
    void sendSingle_rejectsBlacklistedPhone() {
        FakeNotifyGateways gateways = new FakeNotifyGateways();
        gateways.add("13800000000");

        assertThatThrownBy(() -> new SendSmsCmdExe(gateways, gateways).sendSingle(cmd("13800000000")))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("blacklisted");
    }

    private SendSmsCmd cmd(String phone) {
        return new SendSmsCmd(phone, List.of(), "hello", null, null, null, null, null, null);
    }
}
