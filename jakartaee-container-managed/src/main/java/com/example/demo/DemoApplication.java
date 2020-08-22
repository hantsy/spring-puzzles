package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import javax.naming.NamingException;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory")
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
            posts.save(Post.builder().title("Spring and Jakarta EE").body("content of Spring and Jakarta EE").build());
            posts.findAll().forEach(post -> log.info("saved post:{}", post));
        };
    }

    @Bean
    RouterFunction<ServerResponse> router(PostRepository posts, SimplePostRepository simplePosts) {
        return route()
                .GET("/", req -> ok().body(posts.findAll()))
                .GET("/simple", req -> ok().body(simplePosts.findAll()))
                .build();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() throws NamingException {
        JndiObjectFactoryBean ds = new JndiObjectFactoryBean();
        ds.setJndiName("java:jboss/jpa/BlogPU");
        ds.afterPropertiesSet();
        return (EntityManagerFactory) ds.getObject();
    }

    @Bean
    public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

}

@Component
@RequiredArgsConstructor
class SimplePostRepository {

    private final EntityManager entityManager;

    List<Post> findAll() {
        var cb = this.entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Post.class);
        var root = query.from(Post.class);

        return this.entityManager.createQuery(query).getResultList();
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    private String body;

    @CreatedDate
    private LocalDateTime createdAt;
}