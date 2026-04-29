package com.aotemiao.artemis.workflow.infra.dataobject.definition;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("workflow_flow_definitions")
public class FlowDefinitionDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("flow_code")
    private String flowCode;

    @Column("flow_name")
    private String flowName;

    @Column("model_type")
    private String modelType;

    @Column("category_id")
    private Long categoryId;

    @Column("version")
    private Integer version;

    @Column("publish_status")
    private Integer publishStatus;

    @Column("custom_form")
    private Boolean customForm;

    @Column("form_path")
    private String formPath;

    @Column("active_status")
    private Integer activeStatus;

    @Column("listener")
    private String listener;

    @Column("ext_json")
    private String extJson;

    @Column("tenant_id")
    private String tenantId;

    @Column("definition_json")
    private String definitionJson;

    @Column("definition_xml")
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
