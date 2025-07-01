package com.example.demo.tenant.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

class TenantAwareDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return CurrentTenantIdHolder.getTenantId();
    }
}
