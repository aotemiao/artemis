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
- `INTERFACE: com.aotemiao.artemis.resource.client.api.ResourceMessageService`
- `METHOD: PublishedMessageResponse publishToUser(PublishMessageRequest request)`
- `METHOD: List<PublishedMessageResponse> publishToAll(PublishMessageRequest request)`
- `DTO: com.aotemiao.artemis.resource.client.dto.PublishMessageRequest(String title, String content, String sender, Long recipientUserId, List<Long> recipientUserIds, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.PublishedMessageResponse(Long id, Long recipientUserId, String title, Integer readFlag)`
