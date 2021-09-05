package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
@RequestMapping("")
@RequiredArgsConstructor
class SseController {
    private static final Logger log = LoggerFactory.getLogger(SseController.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @GetMapping(value = "events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseMessages() {
        SseEmitter sseEmitter = new SseEmitter(1000L);
        sseEmitter.onCompletion(() -> log.debug("completed"));
        sseEmitter.onError(error -> log.error(error.getMessage()));
        sseEmitter.onTimeout(() -> log.debug("timeout..."));

        //send messages to stream
        sseEmitter.send(event().id("1").data(new TextMessage("message 1"), MediaType.APPLICATION_JSON));
        sseEmitter.send(event().id("2").data(new TextMessage("message 2"), MediaType.APPLICATION_JSON));
        sseEmitter.send(event().id("3").data(new TextMessage("message 3"), MediaType.APPLICATION_JSON));

        // if the emitter does not call `complete`, will fail the tests using `MockMvc`.
        // but in the real world application, we could always keep the sse connection open
        // till the client close it or some exceptions occur.
        sseEmitter.complete();
        return sseEmitter;
    }

    // all sse clients
    Set<SseEmitter> emitters= new LinkedHashSet<>();

    @SneakyThrows
    @GetMapping(value = "broadcast", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter broadcastMessages() {
        SseEmitter sseEmitter = new SseEmitter();
        emitters.add(sseEmitter);

        sseEmitter.onCompletion(() -> {
            log.debug("completed");
            emitters.remove(sseEmitter);
        });
        sseEmitter.onError(error -> log.error(error.getMessage()));
        sseEmitter.onTimeout(() -> {
            log.debug("timeout...");
            emitters.remove(sseEmitter);
        });
        return sseEmitter;
    }

    @Scheduled(initialDelay = 1000L, fixedRate = 500L)
    public void emitMessages(){
        this.emitters.forEach(e -> {
            Set<ResponseBodyEmitter.DataWithMediaType> eventData = event()
                    .id(UUID.randomUUID().toString())
                    .data(new TextMessage("message at " + LocalDateTime.now()), MediaType.APPLICATION_JSON)
                    .build();
            try {
                e.send(eventData);
            } catch (IOException ex) {
                e.completeWithError(ex);
            }
        });
    }

    @GetMapping(value = "events2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SneakyThrows
    public ResponseEntity<ResponseBodyEmitter> sseMessages2() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        emitter.onCompletion(() -> log.debug("completed"));
        emitter.onError(error -> log.error(error.getMessage()));
        emitter.onTimeout(() -> log.debug("timeout..."));

        //send messages to stream
        executor.execute(() -> {
            try {
                emitter.send("data:" + objectMapper.writeValueAsString(new TextMessage("message 1")) + "\n\n");
                emitter.send("data:" + objectMapper.writeValueAsString(new TextMessage("message 2")) + "\n\n");
                emitter.send("data:" + objectMapper.writeValueAsString(new TextMessage("message 3")) + "\n\n");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
        return ok(emitter);
    }
}

record TextMessage(String body) {
}

