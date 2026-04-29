package com.aotemiao.artemis.resource.app.command.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.file.ObjectStorageGateway;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import com.aotemiao.artemis.resource.domain.model.file.StoredObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UploadOssFileCmdExeTest {

    @Mock
    private OssFileGateway ossFileGateway;

    @Mock
    private ObjectStorageGateway objectStorageGateway;

    @InjectMocks
    private UploadOssFileCmdExe uploadOssFileCmdExe;

    @Test
    void execute_whenContentPresent_savesFileRecord() {
        when(objectStorageGateway.store("avatar.png", new byte[] {1, 2}))
                .thenReturn(new StoredObject("stored.png", "png", "/files/stored.png", "2026/stored.png", 2));
        when(ossFileGateway.save(any())).thenAnswer(invocation -> {
            OssFile file = invocation.getArgument(0);
            file.setId(1L);
            return file;
        });

        OssFile result =
                uploadOssFileCmdExe.execute(new UploadOssFileCmd("avatar.png", new byte[] {1, 2}, "admin", "{}"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFileName()).isEqualTo("stored.png");
        assertThat(result.getSuffix()).isEqualTo("png");
        assertThat(result.getProvider()).isEqualTo("LOCAL");
        assertThat(result.getExtJson()).isEqualTo("{}");
    }

    @Test
    void execute_whenContentEmpty_throwsBizException() {
        assertThatThrownBy(
                        () -> uploadOssFileCmdExe.execute(new UploadOssFileCmd("empty.txt", new byte[0], null, null)))
                .isInstanceOf(BizException.class);
    }
}
