package com.aotemiao.artemis.system.adapter.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 后台操作日志记录标记。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLogRecord {

    String title();

    String businessType() default "OTHER";

    String operatorType() default "MANAGE";
}
