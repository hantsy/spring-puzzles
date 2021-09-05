package com.example.demo;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class SseControllerWithOkHttpEventSourceTests {

    @LocalServerPort
    private int port;

    @SneakyThrows
    @Test
    public void testSseEndpoints() {
        List<String> messages = new ArrayList<>();
        EventHandler handler = new EventHandler() {
            @Override
            public void onOpen() throws Exception {
                log.debug("onOpen...");
            }

            @Override
            public void onClosed() throws Exception {
                log.debug("onClosed...");
            }

            @Override
            public void onMessage(String s, MessageEvent messageEvent) throws Exception {
                log.debug("onClosed: {} , {}", s, messageEvent);
                messages.add(messageEvent.getData());
            }

            @Override
            public void onComment(String s) throws Exception {
                log.debug("onComment: {}", s);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("onError: {}", throwable);
            }
        };
        EventSource eventSource = new EventSource.Builder(handler, URI.create("http://localhost:" + port + "/events")).build();
        eventSource.start();

        Thread.sleep(1000L);
        assertThat(messages).contains("{\"body\":\"message 1\"}");
        assertThat(messages).contains("{\"body\":\"message 2\"}");
        assertThat(messages).contains("{\"body\":\"message 3\"}");

        eventSource.close();

    }
}
