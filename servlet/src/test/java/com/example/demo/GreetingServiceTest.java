package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class GreetingServiceTest {

    @Test
    public void testGreetingService() {
        new ApplicationContextRunner()
                .withBean(GreetingService.class)
                .run(context -> assertThat(context.getBean(GreetingService.class)).isNotNull());
    }
}
