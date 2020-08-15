package com.example.demo.orders;

import com.example.demo.Orders;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@PropertySource(value = "classpath:/orders.properties")
@EnableJpaRepositories(
        basePackages = "com.example.demo.orders",
        entityManagerFactoryRef = "ordersEntityManager",
        transactionManagerRef = "ordersTransactionManager"
)
public class OrderConfig {
    @Autowired
    Environment env;

    @Bean
    @Orders
    DataSource ordersDataSource() {
        return DataSourceBuilder.create()
                //.driverClassName(env.getProperty("app.datasources.orders.driverClassName"))
                .url(env.getProperty("app.datasources.orders.url"))
                .username(env.getProperty("app.datasources.orders.username"))
                .password(env.getProperty("app.datasources.orders.password"))
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Orders
    public LocalContainerEntityManagerFactoryBean ordersEntityManager() {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(ordersDataSource());
        em.setPackagesToScan("com.example.demo.orders");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("app.datasources.orders.hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Orders
    @Bean
    public PlatformTransactionManager ordersTransactionManager() {
        var transactionManager = new JpaTransactionManager(ordersEntityManager().getObject());
        return transactionManager;
    }

}

