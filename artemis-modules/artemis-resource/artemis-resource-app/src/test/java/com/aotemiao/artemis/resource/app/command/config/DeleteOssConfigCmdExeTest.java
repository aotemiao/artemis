package com.aotemiao.artemis.resource.app.command.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import org.junit.jupiter.api.Test;

class DeleteOssConfigCmdExeTest {

    @Test
    void execute_rejectsBuiltInConfig() {
        FakeOssConfigGateway gateway = new FakeOssConfigGateway();
        OssConfig config = CreateOssConfigCmdExeTest.existingDefault();
        config.setBuiltIn(1);
        gateway.save(config);

        assertThatThrownBy(() -> new DeleteOssConfigCmdExe(gateway).execute(new DeleteOssConfigCmd(1L)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("Built-in");
    }
}
