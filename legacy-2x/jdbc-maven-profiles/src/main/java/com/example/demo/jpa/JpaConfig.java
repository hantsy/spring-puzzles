package com.example.demo.jpa;


import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;


@EnableJpaAuditing
@EnableJpaRepositories(transactionManagerRef = "jpaTransactionManager")
@EnableTransactionManagement(proxyTargetClass = true)
@Configuration
public class JpaConfig {

    private static final String ENV_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String ENV_HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
    private static final String ENV_HIBERNATE_SHOW_SQL = "hibernate.show_sql";
    private static final String ENV_HIBERNATE_FORMAT_SQL = "hibernate.format_sql";

    @Autowired
    private Environment env;

    @Autowired
    private DataSource dataSource;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.demo");
        emf.setPersistenceProvider(new HibernatePersistenceProvider());
        emf.setJpaProperties(jpaProperties());
        return emf;
    }

    private Properties jpaProperties() {
        Properties extraProperties = new Properties();
        extraProperties.put(ENV_HIBERNATE_FORMAT_SQL, env.getProperty(ENV_HIBERNATE_FORMAT_SQL));
        extraProperties.put(ENV_HIBERNATE_SHOW_SQL, env.getProperty(ENV_HIBERNATE_SHOW_SQL));
        extraProperties.put(ENV_HIBERNATE_HBM2DDL_AUTO, env.getProperty(ENV_HIBERNATE_HBM2DDL_AUTO));
        if (env.getProperty(ENV_HIBERNATE_DIALECT) != null) {
            extraProperties.put(ENV_HIBERNATE_DIALECT, env.getProperty(ENV_HIBERNATE_DIALECT));
        }
        return extraProperties;
    }

    @Bean
    public PlatformTransactionManager jpaTransactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaTransactionManager(entityManagerFactoryBean.getObject());
    }

}

