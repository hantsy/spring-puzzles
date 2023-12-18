package com.example.demo;

import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public Mutiny.SessionFactory sessionFactory() {
        try (var emf = Persistence.createEntityManagerFactory("blogPU")) {
            return emf.unwrap(Mutiny.SessionFactory.class);
        }
    }
}
