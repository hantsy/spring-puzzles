package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SseControllerWithJava11HttpClientTests {

    @LocalServerPort
    private int port;

    @SneakyThrows
    @Test
    public void testSseEndpoints() {

        var client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(5000L)).build();
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/events"))
                .header("Accept", MediaType.TEXT_EVENT_STREAM_VALUE)
                .GET()
                .build();

        var messages = client.send(request, HttpResponse.BodyHandlers.ofLines()).body()
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring(5))
                .toList();

        //Thread.sleep(1000L);
        assertThat(messages).contains("{\"body\":\"message 1\"}");
        assertThat(messages).contains("{\"body\":\"message 2\"}");
        assertThat(messages).contains("{\"body\":\"message 3\"}");

    }
}
