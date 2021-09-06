package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;
import reactor.test.StepVerifier;

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
        FluxExchangeResult<TextMessage> result = this.webClient.get().uri("/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM)
                .returnResult(TextMessage.class);

        StepVerifier.create(result.getResponseBody())
                .consumeNextWith(it -> assertThat(it.body()).isEqualTo("message 1"))
                .consumeNextWith(it -> assertThat(it.body()).isEqualTo("message 2"))
                .consumeNextWith(it -> assertThat(it.body()).isEqualTo("message 3"))
                .thenCancel()
                .verify();
    }
}
