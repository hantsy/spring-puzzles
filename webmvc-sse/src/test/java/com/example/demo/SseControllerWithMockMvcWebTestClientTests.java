package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class SseControllerWithMockMvcWebTestClientTests {
    @Autowired
    WebApplicationContext ctx;

    WebTestClient webClient;

    @BeforeEach
    public void setup() {
        this.webClient = MockMvcWebTestClient.bindToApplicationContext(ctx)
                .build();
    }

    @SneakyThrows
    @Test
    public void testSseEndpoints() {

        this.webClient.get().uri("events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(TextMessage.class)
                .value(messages -> assertThat(messages).containsAll(
                        List.of(
                                new TextMessage("message 1"),
                                new TextMessage("message 2"),
                                new TextMessage("message 3")
                        )
                ));
    }
}
