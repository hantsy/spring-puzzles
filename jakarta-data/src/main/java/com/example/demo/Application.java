package com.example.demo;

import com.example.demo.repository.PostRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@Configuration
public class Application {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Application.class)) {

            var posts = context.getBean(PostRepository.class);
            posts.findAll().forEach(System.out::println);
        }
    }
}
