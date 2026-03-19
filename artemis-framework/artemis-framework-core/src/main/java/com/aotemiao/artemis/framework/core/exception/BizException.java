package com.aotemiao.artemis.framework.core.exception;

import com.aotemiao.artemis.framework.core.constant.ErrorCode;

/** 业务异常，可预期、不重试。例如参数校验失败、业务规则违反。 */
public class BizException extends BaseException {

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BizException(String code, String message) {
        this(code, message, 400);
    }

    public BizException(String code, String message, int httpStatus) {
        super(new ErrorCode() {
            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public int getHttpStatus() {
                return httpStatus;
            }
        });
    }
}
