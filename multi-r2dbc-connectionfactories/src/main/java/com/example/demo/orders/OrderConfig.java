package com.example.demo.orders;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;

import javax.annotation.PostConstruct;

@Configuration
@EnableR2dbcRepositories(entityOperationsRef = "ordersEntityTemplate")
public class OrderConfig {

    @Bean()
    @Qualifier("ordersConnectionFactory")
    public ConnectionFactory ordersConnectionFactory() {
        return ConnectionFactories.get("r2dbc:postgres://user:password@localhost/orders");
    }


    @Bean
    public R2dbcEntityOperations ordersEntityTemplate(@Qualifier("ordersConnectionFactory") ConnectionFactory connectionFactory) {

        DefaultReactiveDataAccessStrategy strategy = new DefaultReactiveDataAccessStrategy(PostgresDialect.INSTANCE);
        DatabaseClient databaseClient = DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .bindMarkers(PostgresDialect.INSTANCE.getBindMarkersFactory())
                .build();

        return new R2dbcEntityTemplate(databaseClient, strategy);
    }


    @PostConstruct
    public void initialize() {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("scripts/orders/schema.sql"),
                new ClassPathResource("scripts/orders/data.sql")
        );
        databasePopulator.populate(ordersConnectionFactory()).subscribe();
    }
}
