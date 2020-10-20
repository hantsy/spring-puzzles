package com.example.greeting.config;

import com.example.greeting.core.GreetingService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
@TestPropertySource(properties = {"greeting.enabled=false"})
@SpringBootTest(classes = EnabledConfigTest.EnabledApp.class)
@Disabled
public class EnabledConfigTest {

    @Autowired(required = false)
    GreetingService greetingService;

    @Test
    public void testGreetingServiceEnabled() {
        assertThat(this.greetingService).isNull();
    }


    @SpringBootApplication
    @EnableGreetingService
    @ComponentScan("com.example.greeting")
    static class EnabledApp {
    }

}


