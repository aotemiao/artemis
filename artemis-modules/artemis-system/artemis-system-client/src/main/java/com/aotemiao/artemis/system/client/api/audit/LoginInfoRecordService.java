package com.aotemiao.artemis.system.client.api.audit;

import com.aotemiao.artemis.system.client.dto.audit.RecordLoginInfoRequest;

/** 登录访问日志远程记录服务。 */
public interface LoginInfoRecordService {

    void record(RecordLoginInfoRequest request);
}
