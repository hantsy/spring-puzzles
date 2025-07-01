package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SseControllerWithRestTemplateTests {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @Autowired
    private RestTemplateBuilder builder;

    @BeforeEach
    public void setup() {
        RestTemplateBuilder restTemplateBuilder = builder.rootUri("http://localhost:" + port)
                .requestFactory(OkHttp3ClientHttpRequestFactory::new)// use OKHttp instead of the default JDK client
                .setReadTimeout(Duration.ofMillis(5000L))
                .setConnectTimeout(Duration.ofMillis(1000L));
        this.restTemplate = new TestRestTemplate(restTemplateBuilder);
    }

    @SneakyThrows
    @Test
    public void testSseEndpoints() {

        var messages = this.restTemplate.execute(
                URI.create("/events"),
                HttpMethod.GET,
                request -> {
                    request.getHeaders().setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                },
                response -> {
                    List<String> result = new ArrayList<>();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            log.debug("reading line: {}", line);
                            if (line.startsWith("data:")) {
                                log.debug("add line to result: {}", line);
                                //remove prefix "data:"
                                String data = line.substring(5);
                                result.add(data);
                            }
                        }
                    }
                    return result;
                }
        );

        Thread.sleep(1000L);
        log.debug("events: {}", messages);

        assertThat(messages).contains("{\"body\":\"message 1\"}");
        assertThat(messages).contains("{\"body\":\"message 2\"}");
        assertThat(messages).contains("{\"body\":\"message 3\"}");
    }
}
