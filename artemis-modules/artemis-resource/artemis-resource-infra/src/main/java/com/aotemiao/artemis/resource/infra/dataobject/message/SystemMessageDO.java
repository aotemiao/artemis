package com.aotemiao.artemis.resource.infra.dataobject.message;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("resource_system_messages")
public class SystemMessageDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("sender")
    private String sender;

    @Column("recipient_user_id")
    private Long recipientUserId;

    @Column("broadcast_flag")
    private Integer broadcastFlag;

    @Column("read_flag")
    private Integer readFlag;

    @Column("read_time")
    private LocalDateTime readTime;

    @Column("ext_json")
    private String extJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public Integer getBroadcastFlag() {
        return broadcastFlag;
    }

    public void setBroadcastFlag(Integer broadcastFlag) {
        this.broadcastFlag = broadcastFlag;
    }

    public Integer getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
    }
}
