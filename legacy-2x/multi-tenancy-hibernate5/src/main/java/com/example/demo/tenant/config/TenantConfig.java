package com.example.demo.tenant.config;

import com.example.demo.master.TenantDataSourceConfigEntity;
import com.example.demo.master.TenantDataSourceConfigRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo.tenant",
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef = "tenantTransactionManager"
)
public class TenantConfig {

    @Autowired
    private Environment env;

    @Autowired
    private TenantDataSourceConfigRepository configRepository;

    private final HashMap<Object, Object> tenantDataSourceMap = new HashMap<>();

    private final DataSource fallbackDataSource = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();

    @PostConstruct
    public void initializeTenantDataSources() {
        this.initializeTenantDataSourceMap();
        this.initializeTenantSampleData();

        // add a default tenant, used an embedded H2.
        tenantDataSourceMap.put("public", this.fallbackDataSource);
    }

    private void initializeTenantSampleData() {
        this.tenantDataSourceMap.keySet().forEach(
                tenantId -> {
                    var scripts = new Resource[]{
                            new ClassPathResource("scripts/" + tenantId + "/schema.sql"),
                            new ClassPathResource("scripts/" + tenantId + "/data.sql")
                    };
                    new ResourceDatabasePopulator(scripts).execute((DataSource) this.tenantDataSourceMap.get(tenantId));
                }
        );
    }

    private void initializeTenantDataSourceMap() {
        for (TenantDataSourceConfigEntity data : configRepository.findAll(Sort.by("tenantId"))) {
            var tenantId = data.getTenantId();
            var url = data.getUrl();
            var username = data.getUsername();
            var password = data.getPassword();
            var driverClassName = data.getDriverClassName();

            var builder = DataSourceBuilder.create()
                    .url(url)
                    .username(username)
                    .password(password)
                    .type(HikariDataSource.class);
            if (StringUtils.hasText(driverClassName)) {
                builder.driverClassName(driverClassName);
            }

            tenantDataSourceMap.putIfAbsent(tenantId, builder.build());
        }

    }

    @Bean
    @Qualifier("tenantEntityManagerFactory")
    @Lazy
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory() {
        var em = new LocalContainerEntityManagerFactoryBean();

        em.setDataSource(this.fallbackDataSource);
        em.setPackagesToScan("com.example.demo.tenant");
        em.setPersistenceUnitName("tenant");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));

        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider(this.tenantDataSourceMap, this.fallbackDataSource));
        properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver());

        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @Qualifier("tenantTransactionManager")
    @Lazy
    public PlatformTransactionManager tenantTransactionManager(
            @Qualifier("tenantEntityManagerFactory")
                    LocalContainerEntityManagerFactoryBean tenantEntityManager) {
        var transactionManager = new JpaTransactionManager(tenantEntityManager.getObject());
        return transactionManager;
    }

    private CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolver() {
            @Override
            public String resolveCurrentTenantIdentifier() {
                var id = CurrentTenantIdHolder.getTenantId();
                if (!StringUtils.hasText(id)) {
                    id = "public";
                }

                return id;
            }

            @Override
            public boolean validateExistingCurrentSessions() {
                return true;
            }
        };
    }

    private MultiTenantConnectionProvider multiTenantConnectionProvider(
            Map<Object, Object> dataSourceMap,
            DataSource fallbackDataSource) {
        return new AbstractDataSourceBasedMultiTenantConnectionProviderImpl() {

            @Override
            protected DataSource selectAnyDataSource() {
                return fallbackDataSource;
            }

            @Override
            protected DataSource selectDataSource(String tenantIdentifier) {
                return (DataSource) dataSourceMap.get(tenantIdentifier);
            }
        };

    }

}



