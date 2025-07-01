package com.example.demo.pong;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@SpringBootApplication
public class PongApplication {
    public static final String TOPIC_PINGPONG = "pingpong";

    public static void main(String[] args) {
        new SpringApplicationBuilder(PongApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}

@Component
@Slf4j
@RequiredArgsConstructor
class PongHandler {
    @KafkaListener(groupId = "pong", topics = PongApplication.TOPIC_PINGPONG)
    @SendTo // use default replyTo expression
    public String handle(String request) {
        log.info("Received: {} in {}", request, this.getClass().getName());
        return "pong at " + LocalDateTime.now();
    }
}