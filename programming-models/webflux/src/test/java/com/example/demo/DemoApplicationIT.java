package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

class DemoApplicationIT {

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    void getAllPosts() {
        this.client.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Post.class).hasSize(2);
    }

}
