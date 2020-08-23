package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jersey.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @Bean
    ResourceConfigCustomizer jerseyConfigCustomizer() {
        return config -> {
            log.info("jersey config: {}", config);
        };
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
