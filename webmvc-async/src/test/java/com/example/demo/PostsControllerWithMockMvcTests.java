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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Slf4j
//see: https://github.com/spring-projects/spring-framework/issues/21408
public class PostsControllerWithMockMvcTests {

    @Autowired
    WebApplicationContext ctx;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.ctx)
                .alwaysDo(log())
                .build();
    }

    @SneakyThrows
    @Test
    public void testGetAllPostsEndpoints() {
        var mvcResult = mockMvc.perform(get("/posts").accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        log.debug("mvcResult.getRequest().isAsyncStarted(): {}", mvcResult.getRequest().isAsyncStarted());
        mvcResult.getAsyncResult(500L);

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        String posts = mvcResult.getResponse().getContentAsString();
        log.debug("posts: {}", posts);
        assertThat(posts).contains("Spring WebMvc");
    }
}
