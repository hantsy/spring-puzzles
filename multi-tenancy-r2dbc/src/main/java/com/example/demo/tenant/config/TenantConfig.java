package com.example.demo.tenant.config;

import com.example.demo.master.TenantConfigRepository;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.example.demo.tenant.repository", entityOperationsRef = "tenantEntityTemplate")
@Slf4j
public class TenantConfig {

    @Autowired
    @Qualifier(value = "masterConnectionFactory")
    ConnectionFactory fallbackConnectionFactory;

    @Autowired
    private TenantConfigRepository configRepository;

    private final HashMap<Object, Object> tenantConnectionFactoriesMap = new HashMap<>();

    @PostConstruct
    public void initializeTenantDataSources() {
//        configRepository.findAll(Sort.by("tenantId"))
//                .doOnNext(
//                        data -> {
//                            var tenantId = data.getTenantId();
//                            var url = data.getUrl();
//                            tenantConnectionFactoriesMap.putIfAbsent(tenantId, ConnectionFactories.get(url));
//                        }
//                )
//                .then()
//                .thenMany(Flux.fromIterable(this.tenantConnectionFactoriesMap.keySet()))
//                .flatMap(tenantId -> {
//                    var scripts = new Resource[]{
//                            new ClassPathResource("scripts/" + tenantId + "/schema.sql"),
//                            new ClassPathResource("scripts/" + tenantId + "/data.sql")
//                    };
//                    return new ResourceDatabasePopulator(scripts)
//                            .populate((ConnectionFactory) this.tenantConnectionFactoriesMap.get(tenantId));
//                })
//                .subscribe(
//                        data -> log.debug("data: {}", data),
//                        error -> log.error("error:" + error)
//                );
        this.initializeTenantConnectionFactoriesMap();
        this.initializeTenantSampleData();
    }

    private void initializeTenantSampleData() {
        this.tenantConnectionFactoriesMap.keySet().forEach(
                tenantId -> {
                    var scripts = new Resource[]{
                            new ClassPathResource("scripts/" + tenantId + "/schema.sql"),
                            new ClassPathResource("scripts/" + tenantId + "/data.sql")
                    };
                    new ResourceDatabasePopulator(scripts)
                            .populate((ConnectionFactory) this.tenantConnectionFactoriesMap.get(tenantId))
                            .block();
                }
        );
    }

    private void initializeTenantConnectionFactoriesMap() {
        configRepository.findAll(Sort.by("tenantId"))
                .doOnNext(
                        data -> {
                            var tenantId = data.getTenantId();
                            var url = data.getUrl();
                            tenantConnectionFactoriesMap.putIfAbsent(tenantId, ConnectionFactories.get(url));
                        }
                )
                .blockLast(Duration.ofSeconds(5));

    }

    @Bean()
    @Qualifier("tenantConnectionFactory")
    public ConnectionFactory tenantConnectionFactory() {
        var tenantConnectionFactory = new TenantAwareConnectionFactory();
        tenantConnectionFactory.setDefaultTargetConnectionFactory(fallbackConnectionFactory);
        tenantConnectionFactory.setTargetConnectionFactories(tenantConnectionFactoriesMap);
        return tenantConnectionFactory;
    }

    @Bean
    public R2dbcEntityOperations tenantEntityTemplate(@Qualifier("tenantConnectionFactory") ConnectionFactory connectionFactory) {

        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        DefaultReactiveDataAccessStrategy strategy = new DefaultReactiveDataAccessStrategy(dialect);
        DatabaseClient databaseClient = DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .bindMarkers(dialect.getBindMarkersFactory())
                .build();

        return new R2dbcEntityTemplate(databaseClient, strategy);
    }
}
