package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GreetingController.class)
class GreetingControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser
    void testHelloEndpoint() throws Exception {
        mvc.perform(get("/hello")
                        .param("name", "Hantsy")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Hantsy"));

    }

    @Test
    void testHelloEndpointWithoutAuth() throws Exception {
        mvc.perform(get("/hello")
                        .param("name", "Hantsy")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());

    }

}
