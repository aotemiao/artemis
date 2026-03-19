package com.aotemiao.artemis.framework.core.constant;

/** 通用错误码。 */
public enum CommonErrorCode implements ErrorCode {
    OK("OK", "success", 200),
    BAD_REQUEST("BAD_REQUEST", "Bad request", 400),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized", 401),
    FORBIDDEN("FORBIDDEN", "Forbidden", 403),
    NOT_FOUND("NOT_FOUND", "Resource not found", 404),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error", 500);

    private final String code;
    private final String message;
    private final int httpStatus;

    CommonErrorCode(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

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
}
