package com.example.demo;

import com.example.demo.jdbc.DataJdbcPostRepository;
import com.example.demo.jpa.DataJpaPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {
    private final DataJpaPostRepository posts;
    private final DataJdbcPostRepository jdbcPosts;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        posts.deleteAll();
        posts.save(Post.builder().title("Inserting data using Spring Data JPA").body("test content").build());
        posts.findAll().forEach(post -> log.info("saved post:{}", post));
    }
}
