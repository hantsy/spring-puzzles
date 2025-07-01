package com.example.greeting.application;

import com.example.greeting.service.api.GreetingService;
import com.example.greeting.service.impl.DefaultGreetingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GreetingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetingApplication.class, args);
    }

    @Bean
    public GreetingService greetingService(){
        return new DefaultGreetingService();
    }
}

