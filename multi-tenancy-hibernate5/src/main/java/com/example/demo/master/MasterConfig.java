package com.example.demo.master;

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
@EnableJpaRepositories(
        basePackages = "com.example.demo.master",
        entityManagerFactoryRef = "masterEntityManagerFactory",
        transactionManagerRef = "masterTransactionManager"
)
public class MasterConfig {

    @Autowired
    Environment env;

    @Bean
    @Qualifier("masterDataSource")
    DataSource masterDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(env.getProperty("master.datasource.driverClassName"))
                .url(env.getProperty("master.datasource.url"))
                .username(env.getProperty("master.datasource.username"))
                .password(env.getProperty("master.datasource.password"))
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("masterEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory(@Qualifier("masterDataSource") DataSource masterDataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(masterDataSource);
        em.setPackagesToScan("com.example.demo.master");
        em.setPersistenceUnitName("master");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("master.datasource.hibernate.dialect"));
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Qualifier("masterTransactionManager")
    @Bean
    public PlatformTransactionManager masterTransactionManager(
            @Qualifier("masterEntityManagerFactory")
                    LocalContainerEntityManagerFactoryBean masterEntityManager) {
        var transactionManager = new JpaTransactionManager(masterEntityManager.getObject());
        return transactionManager;
    }

}

