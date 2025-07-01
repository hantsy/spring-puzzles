package com.example.demo.orders;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
        entityManagerFactoryRef = "ordersEntityManagerFactory",
        transactionManagerRef = "ordersTransactionManager"
)
public class OrderConfig {
    
    @Autowired
    Environment env;

    @Bean
    @Qualifier("ordersDataSource")
    DataSource ordersDataSource() {
        return DataSourceBuilder.create()
                //.driverClassName(env.getProperty("orders.datasource.driverClassName"))
                .url(env.getProperty("orders.datasource.url"))
                .username(env.getProperty("orders.datasource.username"))
                .password(env.getProperty("orders.datasource.password"))
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("ordersEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ordersEntityManagerFactory(@Qualifier("ordersDataSource") DataSource ordersDataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(ordersDataSource());
        em.setPackagesToScan("com.example.demo.orders");
        em.setPersistenceUnitName("orders");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("orders.datasource.hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @Qualifier("ordersTransactionManager")
    public PlatformTransactionManager ordersTransactionManager(
            @Qualifier("ordersEntityManagerFactory")
                    LocalContainerEntityManagerFactoryBean ordersEntityManager) {
        var transactionManager = new JpaTransactionManager(ordersEntityManager.getObject());
        return transactionManager;
    }

}

