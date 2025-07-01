package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Configuration
@Slf4j
public class TenantConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    private void initializeTenantSampleData() {
        List.of("tenant1", "tenant2").forEach(
                tenantId -> {
                    var scripts = new Resource[]{
                            new ClassPathResource("scripts/" + tenantId + "/schema.sql"),
                            new ClassPathResource("scripts/" + tenantId + "/data.sql")
                    };
                    new ResourceDatabasePopulator(scripts).execute(this.dataSource);
                }
        );
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return properties -> {
            properties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, "update");
            properties.put(org.hibernate.cfg.Environment.SHOW_SQL, true);
            properties.put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
            properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider(dataSource));
            properties.put(org.hibernate.cfg.Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver());
        };
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

    private MultiTenantConnectionProvider multiTenantConnectionProvider(DataSource tenantDataSource) {
        return new AbstractDataSourceBasedMultiTenantConnectionProviderImpl() {

            String tenantId;

            @Override
            protected DataSource selectAnyDataSource() {
                this.tenantId = "public";
                return tenantDataSource;
            }

            @Override
            protected DataSource selectDataSource(String tenantIdentifier) {
                log.info("set tenant to schema: {}", tenantIdentifier);
                this.tenantId = tenantIdentifier;
                return tenantDataSource;
            }

            @Override
            public Connection getConnection(String tenantIdentifier) throws SQLException {
                var connection = super.getConnection(tenantIdentifier);
                connection.setSchema(this.tenantId);
                return connection;
            }
        };

/*
        return new AbstractMultiTenantConnectionProvider() {

            @Override
            protected ConnectionProvider getAnyConnectionProvider() {
               return  connectionProvider(dataSource, "public");
            }

            @Override
            protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
                return  connectionProvider(dataSource, tenantIdentifier);
            }
        };
*/

    }

/*
    private ConnectionProvider connectionProvider(DataSource dataSource, String tenantId) {
        var connectionProvider = new DatasourceConnectionProviderImpl() {

            @Override
            public Connection getConnection() throws SQLException {
                var conn = super.getConnection();
                conn.setSchema(tenantId);
                return conn;
            }

        };
        connectionProvider.setDataSource(dataSource);
        return connectionProvider;
    }
*/


}



