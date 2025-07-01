package com.example.demo.model;

import org.hibernate.annotations.Filter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class AbstractEntity implements Serializable, TenantSupport {
    @Column(name = "TENANT_ID")
    private String tenantId = "public";

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
