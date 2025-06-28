package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationWebClientTests {
    @LocalServerPort
    int port;

    private WebClient client;

    @BeforeEach
    void setUp() {
        HttpClient reactorHttpClient = HttpClient.create().wiretap(true);
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(reactorHttpClient);
        this.client = WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .codecs(clientCodecConfigurer -> clientCodecConfigurer
                        .defaultCodecs()
                        .enableLoggingRequestDetails(true)
                )
                .clientConnector(connector)
                .build();
    }

    @Test
    void getAllPosts() {
        this.client.get()
                .uri("/posts")
                .retrieve()
                .bodyToFlux(Post.class)
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getPostByNonExistingId() {
        this.client
                .get().uri("/posts/" + new Random(10).nextLong(10_0000))
                .retrieve()
                .toEntity(Post.class)
                .as(StepVerifier::create)
                .consumeErrorWith(e -> assertThat(e).isInstanceOf(WebClientResponseException.class))
                .verify();
    }

    @Test
    void deletePostByNonExistingId() {
        this.client
                .delete().uri("/posts/" + new Random(10).nextLong(10_0000))
                .retrieve()
                .toBodilessEntity()
                .as(StepVerifier::create)
                .consumeErrorWith(e -> assertThat(e).isInstanceOf(WebClientResponseException.class))
                .verify();
    }

}
