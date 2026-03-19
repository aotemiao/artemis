package com.aotemiao.artemis.framework.core.exception;

import com.aotemiao.artemis.framework.core.constant.ErrorCode;

/** 系统异常，不可预期、可重试。例如 DB 连接失败。 */
public class SysException extends BaseException {

    public SysException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SysException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SysException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
