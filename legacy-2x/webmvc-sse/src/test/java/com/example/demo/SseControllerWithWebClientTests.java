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
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SseControllerWithWebClientTests {

    @LocalServerPort
    private int port;

    private WebClient webClient;

    @BeforeEach
    public void setup() {
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector())
                .codecs(ClientCodecConfigurer::defaultCodecs)
                .exchangeStrategies(ExchangeStrategies.withDefaults())
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @SneakyThrows
    @Test
    public void testSseEndpoints() {

        this.webClient.get().uri("events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchangeToFlux(clientResponse -> clientResponse.bodyToFlux(TextMessage.class))
                .as(StepVerifier::create)
                .consumeNextWith(it -> assertThat(it.body()).isEqualTo("message 1"))
                .consumeNextWith(it -> assertThat(it.body()).isEqualTo("message 2"))
                .consumeNextWith(it -> assertThat(it.body()).isEqualTo("message 3"))
                .thenCancel()
                .verify();
    }
}
