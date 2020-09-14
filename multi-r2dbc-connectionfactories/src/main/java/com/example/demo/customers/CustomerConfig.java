package com.example.demo.customers;

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
@EnableR2dbcRepositories(entityOperationsRef = "customersEntityTemplate")
public class CustomerConfig extends AbstractR2dbcConfiguration {


    @Bean(name = "customersConnectionFactory")
    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:mysql://user:password@localhost/customers");
    }

    @Bean(name = "customersDatabaseClient")
    public DatabaseClient customersDatabaseClient(ConnectionFactory customersConnectionFactory) {
        return DatabaseClient.builder()
                .connectionFactory(customersConnectionFactory)
                .bindMarkers(this.getDialect(customersConnectionFactory).getBindMarkersFactory())
                .build();

    }

    @Override
    @Bean(name = "customersEntityTemplate")
    public R2dbcEntityTemplate r2dbcEntityTemplate(
            @Qualifier("customersDatabaseClient") DatabaseClient databaseClient,
            ReactiveDataAccessStrategy dataAccessStrategy
    ) {
        return super.r2dbcEntityTemplate(databaseClient, dataAccessStrategy);
    }

    @PostConstruct
    public void initialize() {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScripts(
                new ClassPathResource("scripts/customers/schema.sql"),
                new ClassPathResource("scripts/customers/data.sql")
        );
        databasePopulator.populate(connectionFactory()).subscribe();
    }

}
