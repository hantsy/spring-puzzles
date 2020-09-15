package com.example.demo.tenant.config;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public class TenantIdContextHolder {

    public static final String TENANT_ID = "TENANT_ID";

    public static Context withTenantId(String id) {
        return Context.of(TENANT_ID, Mono.just(id));
    }

    public static Mono<String> getTenantId() {
        return Mono.deferContextual(contextView -> {
                    if (contextView.hasKey(TENANT_ID)) {
                        return contextView.get(TENANT_ID);
                    }
                    return Mono.empty();
                }
        );

    }

    public static Function<Context, Context> clearContext() {
        return (context) -> context.delete(TENANT_ID);
    }
}
