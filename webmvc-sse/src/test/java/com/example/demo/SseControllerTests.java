package com.example.demo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Slf4j
public class SseControllerTests {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Autowired
    WebApplicationContext ctx;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.ctx)
                .alwaysDo(log())
                .build();
    }

    // In the SseController, if we do not call `sseEmitter.complete`, this tests always failed.
    @SneakyThrows
    @Test
    public void testSseEndpoints() {
        var mvcResult = mockMvc.perform(get("/events").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        log.debug("mvcResult.getRequest().isAsyncStarted(): {}", mvcResult.getRequest().isAsyncStarted());
        //https://github.com/spring-projects/spring-framework/issues/21408
        mvcResult.getAsyncResult(5000L); // walkaround.

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM));

        String events = mvcResult.getResponse().getContentAsString();
        log.debug("events: {}", events);
        assertThat(events).contains("{\"body\":\"message 1\"}");
    }
}
