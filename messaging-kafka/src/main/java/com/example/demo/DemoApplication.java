package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.LocalDateTime;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.accepted;

@SpringBootApplication
@Slf4j
public class DemoApplication {
    public static final String TOPIC_NAME = "hello";

    public static final String TOPIC_SIMPLE = "simple";

    public static final String TOPIC_LOGGER = "logger";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @Bean
    @SneakyThrows
    RouterFunction<ServerResponse> router(KafkaTemplate<String, Object> kafkaTemplate) {
        return route()
                .GET("/",
                        req -> {
                            kafkaTemplate.send(TOPIC_SIMPLE, "hello world");

                            return accepted().build();
                        }
                )
                .POST("/",
                        req -> {
                            GreetingRequest body = req.body(GreetingRequest.class);
                            kafkaTemplate.send(TOPIC_NAME, body);

                            return accepted().build();
                        }
                )
                .build();
    }

}

@Component
@Slf4j
class SimpleHandler {
    @KafkaListener(topics = DemoApplication.TOPIC_SIMPLE)
    public void handle(String message) {
        log.info("Received request: {} in {}", message, this.getClass().getName());
    }
}

@Component
@Slf4j
class HelloHandler {
    @KafkaListener(topics = DemoApplication.TOPIC_NAME)
    @SendTo(DemoApplication.TOPIC_LOGGER)
    public GreetingResult handle(GreetingRequest request) {
        log.info("Received greeting request: {} in {}", request, this.getClass().getName());
        return GreetingResult.builder()
                .message("Hello, " + request.getName())
                .createdAt(LocalDateTime.now())
                .build();
    }
}

@Component
@Slf4j
class LoggerHandler {
    @KafkaListener(topics = DemoApplication.TOPIC_LOGGER)
    public void handle(GreetingResult result) {
        log.info("Received request: {} in {}", result, this.getClass().getName());
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GreetingRequest {
    String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GreetingResult {
    String message;
    LocalDateTime createdAt;
}