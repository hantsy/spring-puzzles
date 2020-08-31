package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.ParameterizedTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class SignupHandlerTest {

    @Autowired
    private RabbitListenerTestHarness harness;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testTwoWay() throws Exception {
        SignupRequest signupRequest= SignupRequest.builder().fullName("John Doe").phone("5 46 31 71 71").build();
        SignupResult result =this.rabbitTemplate.convertSendAndReceiveAsType(
               DemoApplication.TOPIC_EXCHANGE_NAME,
               DemoApplication.ROUTING_KEY,
               signupRequest,
               ParameterizedTypeReference.forType(SignupResult.class)
       );
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("+33546317171");

        SignupHandler listener = this.harness.getSpy("signup");
        assertNotNull(listener);
        verify(listener).handle(any(SignupRequest.class));
    }

    @TestConfiguration
    @RabbitListenerTest
    static class TestConfig{

    }
}
