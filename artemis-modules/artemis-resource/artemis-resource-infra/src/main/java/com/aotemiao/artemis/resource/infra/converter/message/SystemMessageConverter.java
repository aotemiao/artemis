package com.aotemiao.artemis.resource.infra.converter.message;

import com.aotemiao.artemis.resource.domain.model.message.SystemMessage;
import com.aotemiao.artemis.resource.infra.dataobject.message.SystemMessageDO;

public final class SystemMessageConverter {

    private SystemMessageConverter() {}

    public static SystemMessage toDomain(SystemMessageDO source) {
        SystemMessage target = new SystemMessage();
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setSender(source.getSender());
        target.setRecipientUserId(source.getRecipientUserId());
        target.setBroadcastFlag(source.getBroadcastFlag());
        target.setReadFlag(source.getReadFlag());
        target.setReadTime(source.getReadTime());
        target.setExtJson(source.getExtJson());
        return target;
    }

    public static SystemMessageDO toDO(SystemMessage source) {
        SystemMessageDO target = new SystemMessageDO();
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setSender(source.getSender());
        target.setRecipientUserId(source.getRecipientUserId());
        target.setBroadcastFlag(source.getBroadcastFlag());
        target.setReadFlag(source.getReadFlag());
        target.setReadTime(source.getReadTime());
        target.setExtJson(source.getExtJson());
        return target;
    }
}
