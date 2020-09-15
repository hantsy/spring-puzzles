package com.example.demo.tenant.config;

import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import reactor.core.publisher.Mono;

public class TenantAwareConnectionFactory extends AbstractRoutingConnectionFactory {
    @Override
    protected Mono<Object> determineCurrentLookupKey() {
        return CurrentTenantIdHolder.getTenantId().map(id -> (Object) id);
    }
}
