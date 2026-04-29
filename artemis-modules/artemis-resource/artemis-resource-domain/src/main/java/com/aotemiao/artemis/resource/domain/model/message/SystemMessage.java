package com.aotemiao.artemis.resource.domain.model.message;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 站内消息。 */
public class SystemMessage implements Serializable {

    private Long id;
    private String title;
    private String content;
    private String sender;
    private Long recipientUserId;
    private Integer broadcastFlag;
    private Integer readFlag;
    private LocalDateTime readTime;
    private String extJson;

    public boolean isRead() {
        return Integer.valueOf(1).equals(readFlag);
    }

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
