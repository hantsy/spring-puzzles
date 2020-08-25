package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner init(PostRepository posts) {
        return args -> {
            posts.deleteAll();
            posts.save(Post.builder().title("Configure Oracle DataSource in Apache Tomcat 9").body("test content").build());
            posts.findAll().forEach(post -> log.info("saved post:{}", post));
        };
    }
}

@Component
@RequiredArgsConstructor
class PostRoute extends RouteBuilder {
    private final PostRepository posts;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .bindingMode(RestBindingMode.json);
        rest("/posts")
                .get().produces("application/json")
                .route()
                .log("${body}")
                //.transform().constant("hello world")
                .process(exchange -> {
                    exchange.getIn().setBody(posts.findAll());
                })
                .log("res::${body}");
        //.setHeader(Exchange.HTTP_RESPONSE_CODE).constant("200");
    }
}

interface PostRepository extends JpaRepository<Post, Long> {
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "POSTS")
@EntityListeners(AuditingEntityListener.class)
class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "POSTS_SEQ")
    @SequenceGenerator(sequenceName = "POSTS_SEQ", allocationSize = 1, name = "POSTS_SEQ")
    private Long id;

    private String title;

    private String body;

    @CreatedDate
    private LocalDateTime createdAt;
}
