package com.example.demo.customers;

import com.mysql.cj.jdbc.MysqlXADataSource;
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
@PropertySource(value = "classpath:/customers.properties")
@EnableJpaRepositories(
        basePackages = "com.example.demo.customers",
        entityManagerFactoryRef = "customersEntityManagerFactory"
)
public class CustomerConfig {

    @Autowired
    Environment env;

    @Autowired
    XADataSourceWrapper wrapper;

    @Bean
    @Qualifier("customersDataSource")
    DataSource customersDataSource() throws Exception {
        var ds = new MysqlXADataSource();
        ds.setUrl(env.getProperty("customers.datasource.url"));
        ds.setUser(env.getProperty("customers.datasource.username"));
        ds.setPassword(env.getProperty("customers.datasource.password"));
        return wrapper.wrapDataSource(ds);
    }

    @Bean
    @Qualifier("customersEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean customersEntityManagerFactory(@Qualifier("customersDataSource") DataSource customersDataSource) {
        var em = new LocalContainerEntityManagerFactoryBean();
        em.setJtaDataSource(customersDataSource);
        em.setPackagesToScan("com.example.demo.customers");
        em.setPersistenceUnitName("customers");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("customers.datasource.hibernate.dialect"));

        em.setJpaPropertyMap(properties);

        return em;
    }


}

