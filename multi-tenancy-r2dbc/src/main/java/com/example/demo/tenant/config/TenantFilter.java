package com.example.demo.tenant.config;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TenantFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var value = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
        if (StringUtils.hasText(value)) {
            return chain.filter(exchange)
                    .contextWrite(TenantIdContextHolder.withTenantId(value));
        }
        return chain.filter(exchange);
    }
}
