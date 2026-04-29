package com.aotemiao.artemis.resource.app.command.file;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.resource.domain.gateway.file.ObjectStorageGateway;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteOssFileCmdExeTest {

    @Mock
    private OssFileGateway ossFileGateway;

    @Mock
    private ObjectStorageGateway objectStorageGateway;

    @InjectMocks
    private DeleteOssFileCmdExe deleteOssFileCmdExe;

    @Test
    void execute_deletesObjectAndRecord() {
        OssFile ossFile = new OssFile();
        ossFile.setId(1L);
        ossFile.setObjectKey("2026/file.txt");
        when(ossFileGateway.findById(1L)).thenReturn(Optional.of(ossFile));

        deleteOssFileCmdExe.execute(new DeleteOssFileCmd(1L));

        verify(objectStorageGateway).delete("2026/file.txt");
        verify(ossFileGateway).deleteById(1L);
    }
}
