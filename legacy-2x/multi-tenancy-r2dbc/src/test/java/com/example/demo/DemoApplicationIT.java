package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.CoreMatchers.hasItem;

public class DemoApplicationIT {

    private WebTestClient client;

    @BeforeEach
    public void setup() {
        this.client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    public void willLoadOrders() {
        this.client.get().uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void willLoadCustomers() {
        this.client.get().uri("/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.[*].firstName", hasItem("hantsy@DemoApplication"));
    }

    @Test
    public void willLoadCustomersWithTenant1() {
        this.client.get().uri("/customers")
                .header("X-Tenant-Id","tenant1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.[*].firstName", hasItem("hantsy@tenant1"));
    }

    @Test
    public void willLoadCustomersWithTenant2() {
        this.client.get().uri("/customers")
                .header("X-Tenant-Id","tenant2")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.[*].firstName", hasItem("hantsy@tenant2"));
    }

}