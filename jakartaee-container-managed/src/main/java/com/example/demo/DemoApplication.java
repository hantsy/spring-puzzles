package com.example.demo;

import com.example.demo.ejb.EjbPostRepository;
import com.example.demo.model.Post;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.SimplePostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import javax.ejb.Stateless;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.springframework.context.annotation.ComponentScan.Filter;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory")
@EnableJpaAuditing
@ComponentScan(
        excludeFilters = {
                @Filter(classes = Stateless.class)
        }
)
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
    RouterFunction<ServerResponse> router(
            PostRepository posts,
            SimplePostRepository simplePosts,
            EjbPostRepository ejbPostRepository
    ) {
        return route()
                .GET("/", req -> ok().body(posts.findAll()))
                .GET("/simple", req -> ok().body(simplePosts.findAll()))
                .GET("/ejb", req -> ok().body(ejbPostRepository.findAll()))
                .build();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() throws NamingException {
        JndiObjectFactoryBean emf = new JndiObjectFactoryBean();
        emf.setJndiName("java:jboss/jpa/BlogPU");
        emf.afterPropertiesSet();
        return (EntityManagerFactory) emf.getObject();
    }

    @Bean
    public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    public EjbPostRepository ejbPostRepository() throws NamingException {
        JndiObjectFactoryBean ejb = new JndiObjectFactoryBean();
        ejb.setJndiName("java:module/EjbPostRepositoryBean!com.example.demo.ejb.EjbPostRepository");
        ejb.afterPropertiesSet();
        return (EjbPostRepository) ejb.getObject();
    }
}

