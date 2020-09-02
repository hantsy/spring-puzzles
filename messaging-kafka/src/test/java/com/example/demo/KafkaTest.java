package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static com.example.demo.DemoApplication.TOPIC_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(topics = TOPIC_NAME,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@Import(KafkaTest.TestListener.class)
public class KafkaTest {
    private static final String TOPIC_NAME = "test";

    static {
        System.setProperty(EmbeddedKafkaBroker.BROKER_LIST_PROPERTY, "spring.kafka.bootstrap-servers");
    }

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @SneakyThrows
    public void test() {
        kafkaTemplate.send(TOPIC_NAME, "test message");
        Thread.sleep(1000);

        assertThat(TestListener.result).isEqualTo("test message");
    }

    @TestComponent
    @Slf4j
    static class TestListener {

        static String result = null;

        @KafkaListener(groupId = "a", topics = TOPIC_NAME)
        public void test(String message) {
            log.info("received: {}", message);
            result = message;
        }
    }

}


