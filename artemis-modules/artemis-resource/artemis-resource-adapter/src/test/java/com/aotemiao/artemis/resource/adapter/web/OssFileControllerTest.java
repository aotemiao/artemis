package com.aotemiao.artemis.resource.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.resource.app.command.file.DeleteOssFileCmdExe;
import com.aotemiao.artemis.resource.app.command.file.UploadOssFileCmdExe;
import com.aotemiao.artemis.resource.app.query.file.DownloadOssFileQryExe;
import com.aotemiao.artemis.resource.app.query.file.DownloadedOssFile;
import com.aotemiao.artemis.resource.app.query.file.FindOssFileByIdQryExe;
import com.aotemiao.artemis.resource.app.query.file.ListOssFilesByIdsQryExe;
import com.aotemiao.artemis.resource.app.query.file.OssFilePageQryExe;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OssFileControllerTest {

    private MockMvc mockMvc;

    private UploadOssFileCmdExe uploadOssFileCmdExe;
    private DeleteOssFileCmdExe deleteOssFileCmdExe;
    private FindOssFileByIdQryExe findOssFileByIdQryExe;
    private ListOssFilesByIdsQryExe listOssFilesByIdsQryExe;
    private OssFilePageQryExe ossFilePageQryExe;
    private DownloadOssFileQryExe downloadOssFileQryExe;

    @BeforeEach
    void setUp() {
        uploadOssFileCmdExe = mock(UploadOssFileCmdExe.class);
        deleteOssFileCmdExe = mock(DeleteOssFileCmdExe.class);
        findOssFileByIdQryExe = mock(FindOssFileByIdQryExe.class);
        listOssFilesByIdsQryExe = mock(ListOssFilesByIdsQryExe.class);
        ossFilePageQryExe = mock(OssFilePageQryExe.class);
        downloadOssFileQryExe = mock(DownloadOssFileQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OssFileController(
                        uploadOssFileCmdExe,
                        deleteOssFileCmdExe,
                        findOssFileByIdQryExe,
                        listOssFilesByIdsQryExe,
                        ossFilePageQryExe,
                        downloadOssFileQryExe))
                .build();
    }

    @Test
    void upload_returnsFileRecord() throws Exception {
        when(uploadOssFileCmdExe.execute(any())).thenReturn(sampleFile());

        mockMvc.perform(multipart(OssFileController.BASE_PATH + "/upload")
                        .file(new MockMultipartFile("file", "avatar.png", "image/png", new byte[] {1}))
                        .param("uploader", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.originalFileName").value("avatar.png"));
    }

    @Test
    void page_returnsFileRecords() throws Exception {
        when(ossFilePageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleFile()), 1));

        mockMvc.perform(get(OssFileController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].fileName").value("stored.png"));
    }

    @Test
    void getById_returnsFileRecord() throws Exception {
        when(findOssFileByIdQryExe.execute(any())).thenReturn(Optional.of(sampleFile()));

        mockMvc.perform(get(OssFileController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("/files/stored.png"));
    }

    @Test
    void listByIds_returnsFileRecords() throws Exception {
        when(listOssFilesByIdsQryExe.execute(any())).thenReturn(List.of(sampleFile()));

        mockMvc.perform(get(OssFileController.BASE_PATH + "/by-ids").param("ids", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void download_returnsBinaryContent() throws Exception {
        when(downloadOssFileQryExe.execute(any())).thenReturn(new DownloadedOssFile("stored.png", new byte[] {1, 2}));

        mockMvc.perform(get(OssFileController.BASE_PATH + "/{id}/download", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"stored.png\""))
                .andExpect(content().bytes(new byte[] {1, 2}));
    }

    @Test
    void delete_returnsTrue() throws Exception {
        mockMvc.perform(delete(OssFileController.BASE_PATH + "/{id}", 1L).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    private OssFile sampleFile() {
        OssFile ossFile = new OssFile();
        ossFile.setId(1L);
        ossFile.setFileName("stored.png");
        ossFile.setOriginalFileName("avatar.png");
        ossFile.setSuffix("png");
        ossFile.setUrl("/files/stored.png");
        ossFile.setUploader("admin");
        ossFile.setProvider("LOCAL");
        ossFile.setObjectKey("2026/stored.png");
        ossFile.setSizeBytes(1L);
        ossFile.setExtJson("{}");
        return ossFile;
    }
}
