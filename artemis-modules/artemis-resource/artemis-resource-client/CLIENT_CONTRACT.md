# Resource Client Contract

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

- `INTERFACE: com.aotemiao.artemis.resource.client.api.ping.ResourcePingService`
- `METHOD: PingResponse ping()`
- `DTO: com.aotemiao.artemis.resource.client.dto.ping.PingResponse(String serviceCode, String message)`
- `INTERFACE: com.aotemiao.artemis.resource.client.api.file.ResourceFileService`
- `METHOD: UploadedFileResponse upload(UploadFileRequest request)`
- `METHOD: FileUrlResponse getUrl(Long fileId)`
- `METHOD: List<FileUrlResponse> listByIds(List<Long> fileIds)`
- `DTO: com.aotemiao.artemis.resource.client.dto.file.UploadFileRequest(String originalFileName, byte[] content, String uploader, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.file.UploadedFileResponse(Long id, String fileName, String originalFileName, String url, String provider)`
- `DTO: com.aotemiao.artemis.resource.client.dto.file.FileUrlResponse(Long id, String url)`
- `INTERFACE: com.aotemiao.artemis.resource.client.api.message.ResourceMessageService`
- `METHOD: PublishedMessageResponse publishToUser(PublishMessageRequest request)`
- `METHOD: List<PublishedMessageResponse> publishToAll(PublishMessageRequest request)`
- `DTO: com.aotemiao.artemis.resource.client.dto.message.PublishMessageRequest(String title, String content, String sender, Long recipientUserId, List<Long> recipientUserIds, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.message.PublishedMessageResponse(Long id, Long recipientUserId, String title, Integer readFlag)`
- `INTERFACE: com.aotemiao.artemis.resource.client.api.notify.ResourceSmsService`
- `METHOD: SmsDeliveryResponse sendVerificationCode(SmsVerificationCodeRequest request)`
- `METHOD: SmsDeliveryResponse sendSingle(SmsSendRequest request)`
- `METHOD: List<SmsDeliveryResponse> sendBatch(SmsBatchSendRequest request)`
- `METHOD: SmsDeliveryResponse sendTemplate(SmsTemplateSendRequest request)`
- `METHOD: SmsDeliveryResponse sendAsync(SmsSendRequest request)`
- `METHOD: SmsDeliveryResponse sendDelayed(SmsDelayedSendRequest request)`
- `METHOD: void addBlacklist(String phone)`
- `METHOD: void removeBlacklist(String phone)`
- `INTERFACE: com.aotemiao.artemis.resource.client.api.notify.ResourceEmailService`
- `METHOD: EmailDeliveryResponse sendEmail(EmailSendRequest request)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.SmsVerificationCodeRequest(String phone, String scene, String provider, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.SmsSendRequest(String phone, String content, String provider, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.SmsBatchSendRequest(List<String> phones, String content, String provider, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.SmsTemplateSendRequest(String phone, String templateCode, String templateParams, String provider, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.SmsDelayedSendRequest(String phone, String content, LocalDateTime delayedAt, String provider, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.SmsDeliveryResponse(String messageId, String phone, String provider, String status)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.EmailSendRequest(String to, String subject, String content, String provider, String extJson)`
- `DTO: com.aotemiao.artemis.resource.client.dto.notify.EmailDeliveryResponse(String messageId, String to, String provider, String status)`
