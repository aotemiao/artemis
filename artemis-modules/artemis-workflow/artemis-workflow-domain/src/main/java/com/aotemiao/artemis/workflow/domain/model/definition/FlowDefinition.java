package com.aotemiao.artemis.workflow.domain.model.definition;

import java.io.Serializable;

/** 流程定义。 */
public class FlowDefinition implements Serializable {

    private Long id;
    private String flowCode;
    private String flowName;
    private String modelType;
    private Long categoryId;
    private Integer version;
    private Integer publishStatus;
    private Boolean customForm;
    private String formPath;
    private Integer activeStatus;
    private String listener;
    private String extJson;
    private String tenantId;
    private String definitionJson;
    private String definitionXml;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public Boolean getCustomForm() {
        return customForm;
    }

    public void setCustomForm(Boolean customForm) {
        this.customForm = customForm;
    }

    public String getFormPath() {
        return formPath;
    }

    public void setFormPath(String formPath) {
        this.formPath = formPath;
    }

    public Integer getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Integer activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getListener() {
        return listener;
    }

    public void setListener(String listener) {
        this.listener = listener;
    }

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDefinitionJson() {
        return definitionJson;
    }

    public void setDefinitionJson(String definitionJson) {
        this.definitionJson = definitionJson;
    }

    public String getDefinitionXml() {
        return definitionXml;
    }

    public void setDefinitionXml(String definitionXml) {
        this.definitionXml = definitionXml;
    }
}
