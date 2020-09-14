package com.example.demo.orders;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;

import javax.annotation.PostConstruct;

@Configuration
@EnableR2dbcRepositories(entityOperationsRef = "ordersEntityTemplate")
public class OrderConfig extends AbstractR2dbcConfiguration {

    @Bean(name = "ordersConnectionFactory")
    @Override
    public ConnectionFactory connectionFactory() {
         return ConnectionFactories.get("r2dbc:postgres://user:password@localhost/orders");
//        return new PostgresqlConnectionFactory(
//                PostgresqlConnectionConfiguration.builder()
//                        .host("localhost")
//                        .database("orders")
//                        .username("user")
//                        .password("password")
//                        //.codecRegistrar(EnumCodec.builder().withEnum("post_status", Post.Status.class).build())
//                        .build()
//        );
    }

    @Bean(name = "ordersDatabaseClient")
    public DatabaseClient ordersDatabaseClient(@Qualifier("ordersConnectionFactory") ConnectionFactory ordersConnectionFactory) {
        return DatabaseClient.builder()
                .connectionFactory(ordersConnectionFactory)
                .bindMarkers(this.getDialect(ordersConnectionFactory).getBindMarkersFactory())
                .build();

    }

    @Override
    @Bean(name = "ordersEntityTemplate")
    public R2dbcEntityTemplate r2dbcEntityTemplate(
            @Qualifier("ordersDatabaseClient") DatabaseClient ordersDatabaseClient,
            ReactiveDataAccessStrategy dataAccessStrategy
    ) {
        //return super.r2dbcEntityTemplate(ordersDatabaseClient, dataAccessStrategy);
        return new R2dbcEntityTemplate(ordersDatabaseClient, dataAccessStrategy);
    }

    @PostConstruct
    public void initialize() {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("scripts/orders/schema.sql"),
                new ClassPathResource("scripts/orders/data.sql")
        );
        databasePopulator.populate(connectionFactory()).subscribe();
    }
}
