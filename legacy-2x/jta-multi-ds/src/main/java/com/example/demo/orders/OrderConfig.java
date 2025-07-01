package com.example.demo.orders;

import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@PropertySource(value = "classpath:/orders.properties")
@EnableJpaRepositories(
        basePackages = "com.example.demo.orders",
        entityManagerFactoryRef = "ordersEntityManagerFactory"
)
public class OrderConfig {
    @Autowired
    Environment env;

    @Autowired
    XADataSourceWrapper wrapper;

    @Bean
    @Qualifier("ordersDataSource")
    DataSource ordersDataSource() throws Exception {
        var ds = new PGXADataSource();

        ds.setUrl(env.getProperty("orders.datasource.url"));
        ds.setUser(env.getProperty("orders.datasource.username"));
        ds.setPassword(env.getProperty("orders.datasource.password"));
        return wrapper.wrapDataSource(ds);
    }

    @Bean
    @Qualifier("ordersEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ordersEntityManagerFactory(@Qualifier("ordersDataSource") DataSource ordersDataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setJtaDataSource(ordersDataSource);
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

}

