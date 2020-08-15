package com.example.demo.customers;

import com.example.demo.Customers;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@PropertySource(value = "classpath:/customers.properties")
@EnableJpaRepositories(
        basePackages = "com.example.demo.customers",
        entityManagerFactoryRef = "customersEntityManager",
        transactionManagerRef = "customersTransactionManager"
)
public class CustomerConfig {

    @Autowired
    Environment env;

    @Bean
    @Customers
    DataSource customersDataSource() {
        return DataSourceBuilder.create()
               // .driverClassName(env.getProperty("app.datasources.customers.driverClassName"))
                .url(env.getProperty("app.datasources.customers.url"))
                .username(env.getProperty("app.datasources.customers.username"))
                .password(env.getProperty("app.datasources.customers.password"))
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Customers
    public LocalContainerEntityManagerFactoryBean customersEntityManager() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(customersDataSource());
        em.setPackagesToScan("com.example.demo.customers");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("app.datasources.customers.hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Customers
    @Bean
    public PlatformTransactionManager customersTransactionManager() {
        var transactionManager = new JpaTransactionManager(customersEntityManager().getObject());
        return transactionManager;
    }

}

