package com.example.demo.jdbc;

import com.example.demo.DataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@ComponentScan
@Import({DataSourceConfig.class, JdbcConfig.class})
class TestConfig {
    @Autowired
    DataSource dataSource;

    @PostConstruct
    public void init() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(
                new ClassPathResource("schema.sql"),
                new ClassPathResource("data.sql")
        );
        populator.execute(dataSource);
    }
}
