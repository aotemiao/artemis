package com.aotemiao.artemis.resource.app.query.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.file.ObjectStorageGateway;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.DownloadedObject;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DownloadOssFileQryExeTest {

    @Mock
    private OssFileGateway ossFileGateway;

    @Mock
    private ObjectStorageGateway objectStorageGateway;

    @InjectMocks
    private DownloadOssFileQryExe downloadOssFileQryExe;

    @Test
    void execute_whenFileExists_returnsContent() {
        OssFile ossFile = new OssFile();
        ossFile.setId(1L);
        ossFile.setObjectKey("2026/file.txt");
        when(ossFileGateway.findById(1L)).thenReturn(Optional.of(ossFile));
        when(objectStorageGateway.load("2026/file.txt")).thenReturn(new DownloadedObject("file.txt", new byte[] {1}));

        DownloadedOssFile result = downloadOssFileQryExe.execute(new DownloadOssFileQry(1L));

        assertThat(result.fileName()).isEqualTo("file.txt");
        assertThat(result.content()).containsExactly(1);
    }

    @Test
    void execute_whenRecordMissing_throwsBizException() {
        when(ossFileGateway.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> downloadOssFileQryExe.execute(new DownloadOssFileQry(404L)))
                .isInstanceOf(BizException.class);
    }
}
