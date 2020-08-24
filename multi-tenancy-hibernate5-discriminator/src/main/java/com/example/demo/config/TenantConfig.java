package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;

@Configuration
@Slf4j
public class TenantConfig {

    @Bean
    @ConditionalOnMissingBean
    public PlatformTransactionManager transactionManager(
            ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        JpaTransactionManager transactionManager = new JpaTransactionManager() {
            @Override
            protected EntityManager createEntityManagerForTransaction() {
                final EntityManager entityManager = super.createEntityManagerForTransaction();
                var tenantId = CurrentTenantIdHolder.getTenantId();

                if (StringUtils.hasText(tenantId)) {
                    Session session = entityManager.unwrap(Session.class);
                    session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
                }

                return entityManager;
            }
        };
        transactionManagerCustomizers.ifAvailable((customizers) -> customizers.customize(transactionManager));

        return transactionManager;
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return properties -> {
            // DISCRIMINATOR based multi-tenant is NOT implemented yet.
            // see: https://hibernate.atlassian.net/browse/HHH-6054
            //properties.put(org.hibernate.cfg.Environment.MULTI_TENANT, MultiTenancyStrategy.DISCRIMINATOR);
        };
    }

}



