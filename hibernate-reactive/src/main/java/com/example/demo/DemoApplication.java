package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.persistence.Persistence;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication(exclude = R2dbcAutoConfiguration.class)
@Slf4j
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public Mutiny.SessionFactory sessionFactory() {
        return Persistence.createEntityManagerFactory("blogPU")
            .unwrap(Mutiny.SessionFactory.class);
    }

    @Bean
    public RouterFunction<ServerResponse> routes(PostsHandler handler) {
        return route(GET("/posts"), handler::all)
            .andRoute(POST("/posts"), handler::create)
            .andRoute(GET("/posts/{id}"), handler::get)
            .andRoute(PUT("/posts/{id}"), handler::update)
            .andRoute(DELETE("/posts/{id}"), handler::delete);
    }
}
