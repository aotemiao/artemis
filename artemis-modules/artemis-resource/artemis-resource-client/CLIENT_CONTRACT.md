# Resource Client Contract

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

- `INTERFACE: com.aotemiao.artemis.resource.client.api.ResourcePingService`
- `METHOD: PingResponse ping()`
- `DTO: com.aotemiao.artemis.resource.client.dto.PingResponse(String serviceCode, String message)`
- `INTERFACE: com.aotemiao.artemis.resource.client.api.ResourceFileService`
- `METHOD: UploadedFileResponse upload(UploadFileRequest request)`
- `METHOD: FileUrlResponse getUrl(Long fileId)`
- `METHOD: List<FileUrlResponse> listByIds(List<Long> fileIds)`
- `DTO: com.aotemiao.artemis.resource.client.dto.UploadFileRequest(String originalFileName, byte[] content, String uploader, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.UploadedFileResponse(Long id, String fileName, String originalFileName, String url, String provider)`
- `DTO: com.aotemiao.artemis.resource.client.dto.FileUrlResponse(Long id, String url)`
