package com.aotemiao.artemis.system.infra.dataobject.tenant;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("system_tenants")
public class TenantDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("tenant_no")
    private String tenantNo;

    @Column("company_name")
    private String companyName;

    @Column("contact_name")
    private String contactName;

    @Column("contact_phone")
    private String contactPhone;

    @Column("social_credit_code")
    private String socialCreditCode;

    @Column("address")
    private String address;

    @Column("domain")
    private String domain;

    @Column("intro")
    private String intro;

    @Column("package_id")
    private Long packageId;

    @Column("expire_time")
    private LocalDateTime expireTime;

    @Column("user_limit")
    private Integer userLimit;

    @Column("status")
    private String status;

    @Column("remarks")
    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantNo() {
        return tenantNo;
    }

    public void setTenantNo(String tenantNo) {
        this.tenantNo = tenantNo;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getSocialCreditCode() {
        return socialCreditCode;
    }

    public void setSocialCreditCode(String socialCreditCode) {
        this.socialCreditCode = socialCreditCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(Integer userLimit) {
        this.userLimit = userLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
