package com.aotemiao.artemis.resource.app.command.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import org.junit.jupiter.api.Test;

class SetDefaultOssConfigCmdExeTest {

    @Test
    void execute_rejectsDisabledConfig() {
        FakeOssConfigGateway gateway = new FakeOssConfigGateway();
        OssConfig config = CreateOssConfigCmdExeTest.existingDefault();
        config.setStatus(0);
        config.setDefaultFlag(0);
        gateway.save(config);

        assertThatThrownBy(() -> new SetDefaultOssConfigCmdExe(gateway).execute(new SetDefaultOssConfigCmd(1L)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Disabled");
    }

    @Test
    void execute_setsOnlyOneDefault() {
        FakeOssConfigGateway gateway = new FakeOssConfigGateway();
        gateway.save(CreateOssConfigCmdExeTest.existingDefault());
        OssConfig second = CreateOssConfigCmdExeTest.existingDefault();
        second.setConfigKey("aliyun");
        second.setDefaultFlag(0);
        gateway.save(second);

        new SetDefaultOssConfigCmdExe(gateway).execute(new SetDefaultOssConfigCmd(2L));

        assertThat(gateway.findById(1L))
                .get()
                .extracting(OssConfig::getDefaultFlag)
                .isEqualTo(0);
        assertThat(gateway.findById(2L))
                .get()
                .extracting(OssConfig::getDefaultFlag)
                .isEqualTo(1);
    }
}
