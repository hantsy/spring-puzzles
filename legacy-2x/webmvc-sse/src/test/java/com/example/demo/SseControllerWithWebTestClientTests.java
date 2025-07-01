package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SseControllerWithWebTestClientTests {

    @LocalServerPort
    private int port;

    private WebTestClient webClient;

    @BeforeEach
    public void setup() {
        this.webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @SneakyThrows
    @Test
    public void testSseEndpoints() {

        this.webClient.get().uri("events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
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
