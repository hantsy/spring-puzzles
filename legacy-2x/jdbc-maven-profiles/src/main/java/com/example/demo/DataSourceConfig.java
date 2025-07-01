package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.sql.DataSource;

@Configuration
@PropertySource(value = "classpath:/datasource.properties", ignoreResourceNotFound = true)
public class DataSourceConfig {

    private static final String ENV_DATASOURCE_PASSWORD = "datasource.password";
    private static final String ENV_DATASOURCE_USERNAME = "datasource.username";
    private static final String ENV_DATASOURCE_URL = "datasource.url";
    private static final String ENV_DATASOURCE_JNDINAME = "datasource.jndi-name";

    @Autowired
    private Environment env;

    @Bean
    @Profile("h2")
    public DataSource embeddedDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
    }

    @Bean
    @Profile({"mysql", "pg"})
    public DataSource driverManagerDataSource() {
        DriverManagerDataSource bds = new DriverManagerDataSource();
        //bds.setDriverClassName("com.mysql.jdbc.Driver");
        bds.setUrl(env.getProperty(ENV_DATASOURCE_URL));
        bds.setUsername(env.getProperty(ENV_DATASOURCE_USERNAME));
        bds.setPassword(env.getProperty(ENV_DATASOURCE_PASSWORD));
        return bds;
    }

    @Bean
    @Profile("jndi")
    public DataSource jndiDataSource() throws NamingException {
        JndiObjectFactoryBean ds = new JndiObjectFactoryBean();
        ds.setJndiName(env.getProperty(ENV_DATASOURCE_JNDINAME));
        ds.afterPropertiesSet();

        return (DataSource) ds.getObject();
    }

}
