package com.aotemiao.artemis.resource.app.command.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import org.junit.jupiter.api.Test;

class CreateOssConfigCmdExeTest {

    @Test
    void execute_createsConfigAndClearsOtherDefaults() {
        FakeOssConfigGateway gateway = new FakeOssConfigGateway();
        gateway.save(existingDefault());
        CreateOssConfigCmdExe exe = new CreateOssConfigCmdExe(gateway);

        OssConfig created = exe.execute(new CreateOssConfigCmd(payload("aliyun", 1)));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getProvider()).isEqualTo("ALIYUN");
        assertThat(gateway.findById(1L))
                .get()
                .extracting(OssConfig::getDefaultFlag)
                .isEqualTo(0);
    }

    @Test
    void execute_rejectsDuplicatedConfigKey() {
        FakeOssConfigGateway gateway = new FakeOssConfigGateway();
        gateway.save(existingDefault());
        CreateOssConfigCmdExe exe = new CreateOssConfigCmdExe(gateway);

        assertThatThrownBy(() -> exe.execute(new CreateOssConfigCmd(payload("local", 0))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("already exists");
    }

    static OssConfigPayload payload(String key, Integer defaultFlag) {
        return new OssConfigPayload(
                key,
                "access",
                "secret",
                "bucket",
                "prefix",
                "endpoint",
                "cdn.example.com",
                true,
                "cn-hz",
                "private",
                1,
                defaultFlag,
                0,
                key,
                "{}");
    }

    static OssConfig existingDefault() {
        OssConfig config = new OssConfig();
        config.setConfigKey("local");
        config.setAccessKey("access");
        config.setSecretKey("secret");
        config.setBucket("bucket");
        config.setHttpsEnabled(true);
        config.setAccessPolicy("PRIVATE");
        config.setStatus(1);
        config.setDefaultFlag(1);
        config.setBuiltIn(0);
        config.setProvider("LOCAL");
        return config;
    }
}
