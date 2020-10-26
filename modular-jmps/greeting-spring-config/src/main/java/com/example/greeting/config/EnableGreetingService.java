package com.example.greeting.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@ImportAutoConfiguration(classes = GreetingServiceAutoConfiguration.class)
public @interface EnableGreetingService {
}
