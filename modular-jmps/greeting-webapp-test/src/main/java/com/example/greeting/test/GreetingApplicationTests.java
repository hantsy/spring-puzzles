package com.example.greeting.test;

import com.example.greeting.app.GreetingApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = GreetingApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class GreetingApplicationTests {

    @LocalServerPort
    int port;

    WebTestClient client;

    @BeforeEach
    public void setUp() {
        this.client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void testGreeting() {
        this.client.get()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/")
                                .queryParam("name", "Hantsy")
                                .build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(s -> assertThat(s).contains("Hantsy"));
    }

}
