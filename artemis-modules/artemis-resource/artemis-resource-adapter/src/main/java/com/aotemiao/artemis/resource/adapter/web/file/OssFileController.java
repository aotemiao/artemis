package com.aotemiao.artemis.resource.adapter.web.file;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.adapter.web.dto.file.OssFileDTO;
import com.aotemiao.artemis.resource.app.command.file.DeleteOssFileCmd;
import com.aotemiao.artemis.resource.app.command.file.DeleteOssFileCmdExe;
import com.aotemiao.artemis.resource.app.command.file.UploadOssFileCmd;
import com.aotemiao.artemis.resource.app.command.file.UploadOssFileCmdExe;
import com.aotemiao.artemis.resource.app.query.file.DownloadOssFileQry;
import com.aotemiao.artemis.resource.app.query.file.DownloadOssFileQryExe;
import com.aotemiao.artemis.resource.app.query.file.DownloadedOssFile;
import com.aotemiao.artemis.resource.app.query.file.FindOssFileByIdQry;
import com.aotemiao.artemis.resource.app.query.file.FindOssFileByIdQryExe;
import com.aotemiao.artemis.resource.app.query.file.ListOssFilesByIdsQry;
import com.aotemiao.artemis.resource.app.query.file.ListOssFilesByIdsQryExe;
import com.aotemiao.artemis.resource.app.query.file.OssFilePageQry;
import com.aotemiao.artemis.resource.app.query.file.OssFilePageQryExe;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** OSS 文件 REST API。 */
@RestController
@RequestMapping(OssFileController.BASE_PATH)
public class OssFileController {

    public static final String BASE_PATH = "/api/resource/oss-files";

    private final UploadOssFileCmdExe uploadOssFileCmdExe;
    private final DeleteOssFileCmdExe deleteOssFileCmdExe;
    private final FindOssFileByIdQryExe findOssFileByIdQryExe;
    private final ListOssFilesByIdsQryExe listOssFilesByIdsQryExe;
    private final OssFilePageQryExe ossFilePageQryExe;
    private final DownloadOssFileQryExe downloadOssFileQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this controller does not expose them.")
    public OssFileController(
            UploadOssFileCmdExe uploadOssFileCmdExe,
            DeleteOssFileCmdExe deleteOssFileCmdExe,
            FindOssFileByIdQryExe findOssFileByIdQryExe,
            ListOssFilesByIdsQryExe listOssFilesByIdsQryExe,
            OssFilePageQryExe ossFilePageQryExe,
            DownloadOssFileQryExe downloadOssFileQryExe) {
        this.uploadOssFileCmdExe = uploadOssFileCmdExe;
        this.deleteOssFileCmdExe = deleteOssFileCmdExe;
        this.findOssFileByIdQryExe = findOssFileByIdQryExe;
        this.listOssFilesByIdsQryExe = listOssFilesByIdsQryExe;
        this.ossFilePageQryExe = ossFilePageQryExe;
        this.downloadOssFileQryExe = downloadOssFileQryExe;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<OssFileDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String uploader,
            @RequestParam(required = false) String extJson)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Upload file must not be empty");
        }
        OssFile ossFile = uploadOssFileCmdExe.execute(
                new UploadOssFileCmd(file.getOriginalFilename(), file.getBytes(), uploader, extJson));
        return R.ok(toDTO(ossFile));
    }

    @GetMapping
    public R<PageResult<OssFileDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<OssFile> pageResult = ossFilePageQryExe.execute(new OssFilePageQry(new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(this::toDTO).toList(),
                pageResult.totalPages()));
    }

    @GetMapping("/{id}")
    public R<OssFileDTO> getById(@PathVariable Long id) {
        return R.ok(toDTO(findOssFileByIdQryExe
                .execute(new FindOssFileByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "OssFile not found: " + id))));
    }

    @GetMapping("/by-ids")
    public R<List<OssFileDTO>> listByIds(@RequestParam List<Long> ids) {
        return R.ok(listOssFilesByIdsQryExe.execute(new ListOssFilesByIdsQry(ids)).stream()
                .map(this::toDTO)
                .toList());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        DownloadedOssFile downloaded = downloadOssFileQryExe.execute(new DownloadOssFileQry(id));
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(downloaded.fileName())
                                .build()
                                .toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloaded.content());
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        deleteOssFileCmdExe.execute(new DeleteOssFileCmd(id));
        return R.ok(true);
    }

    private OssFileDTO toDTO(OssFile ossFile) {
        return new OssFileDTO(
                ossFile.getId(),
                ossFile.getFileName(),
                ossFile.getOriginalFileName(),
                ossFile.getSuffix(),
                ossFile.getUrl(),
                ossFile.getUploader(),
                ossFile.getProvider(),
                ossFile.getSizeBytes(),
                ossFile.getExtJson());
    }
}
