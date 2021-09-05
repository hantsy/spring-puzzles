package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.SseEventSource;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SseControllerWithJerseyClientTests {

    @LocalServerPort
    private int port;

    private Client client;


    @BeforeEach
    public void setup() {
        this.client = ClientBuilder.newBuilder()
                .register(SseFeature.class)
                .build();
    }

    @SneakyThrows
    @Test
    public void testSseEndpoints() {
        List<String> messages = new ArrayList<>();
        WebTarget target = this.client.target("http://localhost:" + port + "/events");
        SseEventSource sseEventSource = SseEventSource.target(target).build();
        sseEventSource.register(event -> messages.add(event.readData()));
        sseEventSource.open();

        Thread.sleep(1000L);
        assertThat(messages).contains("{\"body\":\"message 1\"}");
        assertThat(messages).contains("{\"body\":\"message 2\"}");
        assertThat(messages).contains("{\"body\":\"message 3\"}");
        sseEventSource.close();
    }
}
