package com.aotemiao.artemis.system.domain.model.tenant;

import java.io.Serializable;
import java.util.List;

/** 租户套餐。 */
public class TenantPackage implements Serializable {

    private Long id;
    private String packageName;
    private boolean menuCheckStrictly = true;
    private boolean enabled = true;
    private String remarks;
    private List<Long> menuIds = List.of();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isMenuCheckStrictly() {
        return menuCheckStrictly;
    }

    public void setMenuCheckStrictly(boolean menuCheckStrictly) {
        this.menuCheckStrictly = menuCheckStrictly;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<Long> getMenuIds() {
        return List.copyOf(menuIds);
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }
}
