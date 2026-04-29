package com.aotemiao.artemis.workflow.domain.model.spel;

import java.io.Serializable;

/** 流程 SpEL 表达式。 */
public class FlowSpel implements Serializable {

    private Long id;
    private String componentName;
    private String methodName;
    private String parameters;
    private String previewExpression;
    private String remarks;
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getPreviewExpression() {
        return previewExpression;
    }

    public void setPreviewExpression(String previewExpression) {
        this.previewExpression = previewExpression;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
