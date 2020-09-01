package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class GreetingHandlerTest {

    @Autowired
    private RabbitListenerTestHarness harness;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testTwoWay() throws Exception {
        GreetingRequest signupRequest = GreetingRequest.builder().name("Hantsy").build();
        this.rabbitTemplate.convertAndSend(
                DemoApplication.TOPIC_EXCHANGE_GREETING,
                DemoApplication.ROUTING_KEY_WELCOME,
                signupRequest
        );

        Thread.sleep(1000);

        WelcomeHandler listener = this.harness.getSpy("welcome");
        assertNotNull(listener);
        verify(listener).welcome(any(GreetingRequest.class));

        GreetingHandler greetingHandler = this.harness.getSpy("greeting");
        assertNotNull(greetingHandler);
        verify(greetingHandler).handle(any(GreetingRequest.class));
    }

    @TestConfiguration
    @RabbitListenerTest
    static class TestConfig {

    }
}
