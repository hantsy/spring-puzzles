package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JmsTest {

    @Autowired
    private JmsTemplate template;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    public void testJmsConfig() {
        assertThat(template).isNotNull();
        assertThat(connectionFactory).isNotNull();
    }

}
