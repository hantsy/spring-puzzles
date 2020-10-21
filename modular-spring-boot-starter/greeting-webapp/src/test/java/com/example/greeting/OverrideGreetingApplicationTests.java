package com.example.greeting;

import com.example.greeting.exclude.ExcludeDefaultGreetingServiceApplication;
import com.example.greeting.override.OverrideDefaultGreetingServiceApplication;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OverrideDefaultGreetingServiceApplication.class)
@AutoConfigureMockMvc
public class OverrideGreetingApplicationTests {


    @Autowired
    MockMvc mockMvc;

    @Test
    public void testGreeting() throws Exception {
        this.mockMvc
                .perform(
                        get("/")
                                .queryParam("name", "Hantsy")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("PrimaryGreetingService")));
    }

}
