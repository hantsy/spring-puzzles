package com.example.demo.tenant.config;

import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import reactor.core.publisher.Mono;

public class TenantAwareConnectionFactory extends AbstractRoutingConnectionFactory {
    @Override
    protected Mono<Object> determineCurrentLookupKey() {
        return TenantIdContextHolder.getTenantId().map(id -> (Object) id);
    }
}
