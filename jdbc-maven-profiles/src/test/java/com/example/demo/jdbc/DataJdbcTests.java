package com.example.demo.jdbc;

import com.example.demo.Post;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("h2")
@Slf4j
public class DataJdbcTests {

    @Autowired
    DataJdbcPostRepository posts;

    @BeforeEach
    public void setup() {
        posts.deleteAll();
    }

    @Test
    public void testInsertAndQuery() {
        posts.save(Post.builder().title("mytest").body("my content").build());
        var result = posts.findAll();
        result.forEach(post -> {
            assertThat(post.getTitle()).isEqualTo("mytest");
            assertNotNull(post.getCreatedAt());
            assertNotNull(post.getUpdatedAt());
        });
    }
}
