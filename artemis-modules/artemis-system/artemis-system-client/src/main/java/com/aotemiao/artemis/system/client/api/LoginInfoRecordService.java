package com.aotemiao.artemis.system.client.api;

import com.aotemiao.artemis.system.client.dto.RecordLoginInfoRequest;

/** 登录访问日志远程记录服务。 */
public interface LoginInfoRecordService {

    void record(RecordLoginInfoRequest request);
}
