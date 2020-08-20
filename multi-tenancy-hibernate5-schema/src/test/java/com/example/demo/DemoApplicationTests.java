package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DemoApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void testCustomersEndpoint() throws Exception {
        mvc
                .perform(
                        get("/customers").accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

    }

    @Test
    void testCustomersFromTenant1Endpoint() throws Exception {
        mvc
                .perform(
                        get("/customers")
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-TenantId", "tenant1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName", containsString("@tenant1")));

    }

    @Test
    void testCustomersFromTenant2Endpoint() throws Exception {
        mvc
                .perform(
                        get("/customers")
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-TenantId", "tenant2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName", containsString("@tenant2")));

    }


    @Test
    void testOrdersEndpoint() throws Exception {
        mvc
                .perform(
                        get("/orders").accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

    }

}
