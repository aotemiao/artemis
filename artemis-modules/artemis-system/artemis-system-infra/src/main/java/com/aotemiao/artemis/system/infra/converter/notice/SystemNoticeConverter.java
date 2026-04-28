package com.aotemiao.artemis.system.infra.converter.notice;

import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import com.aotemiao.artemis.system.infra.dataobject.notice.SystemNoticeDO;

/** 系统通知公告转换器。 */
public final class SystemNoticeConverter {

    private SystemNoticeConverter() {}

    public static SystemNoticeDO toDO(SystemNotice systemNotice) {
        SystemNoticeDO d = new SystemNoticeDO();
        d.setId(systemNotice.getId());
        d.setNoticeTitle(systemNotice.getNoticeTitle());
        d.setNoticeType(systemNotice.getNoticeType());
        d.setNoticeContent(systemNotice.getNoticeContent());
        d.setStatus(systemNotice.getStatus());
        d.setRemarks(systemNotice.getRemarks());
        return d;
    }

    public static SystemNotice toDomain(SystemNoticeDO d) {
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setId(d.getId());
        systemNotice.setNoticeTitle(d.getNoticeTitle());
        systemNotice.setNoticeType(d.getNoticeType());
        systemNotice.setNoticeContent(d.getNoticeContent());
        systemNotice.setStatus(d.getStatus());
        systemNotice.setRemarks(d.getRemarks());
        return systemNotice;
    }
}
