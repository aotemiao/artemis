package com.aotemiao.artemis.framework.web.handler;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BaseException;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.framework.core.exception.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理：将异常映射为统一 R 响应。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBiz(BizException e) {
        LOGGER.warn("BizException: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(R.fail(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(SysException.class)
    public ResponseEntity<R<Void>> handleSys(SysException e) {
        LOGGER.error("SysException: {} - {}", e.getErrorCode().getCode(), e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(R.fail(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<R<Void>> handleBase(BaseException e) {
        LOGGER.warn("BaseException: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(R.fail(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleOther(Exception e) {
        LOGGER.error("Unhandled exception", e);
        return ResponseEntity.status(CommonErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(R.fail(CommonErrorCode.INTERNAL_ERROR.getCode(), CommonErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
