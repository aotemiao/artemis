package com.aotemiao.artemis.workflow.client.dto;

/** artemis-workflow 的最小状态 DTO。 */
public record PingResponse(String serviceCode, String capability, String message) {}
