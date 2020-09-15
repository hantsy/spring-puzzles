package com.example.demo.master;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;

import javax.annotation.PostConstruct;

@Configuration
@EnableR2dbcRepositories(entityOperationsRef = "masterEntityTemplate")
public class MasterConfig {


    @Bean
    @Qualifier(value = "masterConnectionFactory")
    public ConnectionFactory masterConnectionFactory() {
        return ConnectionFactories.get("r2dbc:mysql://user:password@localhost/master");
    }

    @Bean
    public R2dbcEntityOperations masterEntityTemplate(@Qualifier("masterConnectionFactory") ConnectionFactory connectionFactory) {

        DefaultReactiveDataAccessStrategy strategy = new DefaultReactiveDataAccessStrategy(MySqlDialect.INSTANCE);
        DatabaseClient databaseClient = DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .bindMarkers(MySqlDialect.INSTANCE.getBindMarkersFactory())
                .build();

        return new R2dbcEntityTemplate(databaseClient, strategy);
    }

//    @PostConstruct
//    public void initialize() {
//        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
//        databasePopulator.addScripts(
//                new ClassPathResource("scripts/master/schema.sql"),
//                new ClassPathResource("scripts/master/data.sql")
//        );
//        databasePopulator.populate(masterConnectionFactory()).subscribe();
//    }

}
