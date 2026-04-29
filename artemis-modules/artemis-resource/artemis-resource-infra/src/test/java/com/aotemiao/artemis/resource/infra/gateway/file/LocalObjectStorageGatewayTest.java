package com.aotemiao.artemis.resource.infra.gateway.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aotemiao.artemis.framework.core.exception.BizException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalObjectStorageGatewayTest {

    @TempDir
    private java.nio.file.Path tempDir;

    @Test
    void storeLoadAndDelete_roundTripsContent() {
        LocalObjectStorageGateway gateway = new LocalObjectStorageGateway(tempDir.toString(), "/files");

        var stored = gateway.store("avatar.PNG", new byte[] {1, 2, 3});
        var downloaded = gateway.load(stored.objectKey());
        gateway.delete(stored.objectKey());

        assertThat(stored.suffix()).isEqualTo("png");
        assertThat(stored.url()).startsWith("/files/");
        assertThat(downloaded.content()).containsExactly(1, 2, 3);
        assertThat(Files.exists(tempDir.resolve(stored.objectKey()))).isFalse();
    }

    @Test
    void load_whenMissing_throwsBizException() {
        LocalObjectStorageGateway gateway = new LocalObjectStorageGateway(tempDir.toString(), "/files");

        assertThatThrownBy(() -> gateway.load("missing.txt")).isInstanceOf(BizException.class);
    }
}
