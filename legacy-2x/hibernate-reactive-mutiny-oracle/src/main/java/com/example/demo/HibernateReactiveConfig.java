package com.example.demo;

import jakarta.persistence.Persistence;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class HibernateReactiveConfig {
    static final String DEFAULT_PERSISTENCE_UNIT_NAME = "blogPU";

    @Bean
    public Mutiny.SessionFactory sessionFactory() {
        return Persistence.createEntityManagerFactory(DEFAULT_PERSISTENCE_UNIT_NAME)
            .unwrap(Mutiny.SessionFactory.class);
    }
}
