package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import javax.persistence.*;
import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.*;

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
    TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                // fixes naming exception:
                // javax.naming.NamingException: No naming context bound to this class loader
                // root cause:
                // Failed to instantiate [javax.sql.DataSource]: Factory method 'jndiDataSource' threw exception;
                // nested exception is javax.naming.NoInitialContextException: Need to specify class name in environment
                // or system property, or in an application resource file: java.naming.factory.initial
                tomcat.enableNaming();
                return super.getTomcatWebServer(tomcat);
            }

            // use WebServerFactoryCustomizer is more reasonable.
            //
            /*@Override
            protected void postProcessContext(Context context) {

                var resource = new ContextResource();

                resource.setType(DataSource.class.getName());
                resource.setName("jdbc/testDS");
                resource.setAuth("Container");

                // use tomcat-dbcp.
                resource.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
                resource.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
                resource.setProperty("url", "jdbc:oracle:thin:@localhost:1521:xe");
                resource.setProperty("username", "system");
                resource.setProperty("password", "Passw0rd");
                context.getNamingResources().addResource(resource);
            }*/
        };
    }

    // see: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-configure-webserver
    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatFactoryCustomizer(){
        return factory-> {

            TomcatContextCustomizer dataSourceContextCustomizer = context -> {
                var resource = new ContextResource();

                resource.setType(DataSource.class.getName());
                resource.setName("jdbc/testDS");
                resource.setAuth("Container");

                // use tomcat-dbcp.
                resource.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
                resource.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
                resource.setProperty("url", "jdbc:oracle:thin:@localhost:1521:xe");
                resource.setProperty("username", "system");
                resource.setProperty("password", "Passw0rd");
                context.getNamingResources().addResource(resource);
            };

            factory.addContextCustomizers(dataSourceContextCustomizer);
        };
    }

    // Instead, set a *spring.datasource.jndi-name* property in the application.properties.
    //
    /*@Bean
    public DataSource jndiDataSource() throws IllegalArgumentException, NamingException {
        var bean = new JndiObjectFactoryBean();
        bean.setJndiName("java:/comp/env/jdbc/testDS");
        bean.afterPropertiesSet();

        return (DataSource) bean.getObject();
    }*/

    @Bean
    RouterFunction<ServerResponse> router(PostRepository posts) {
        return route(GET("/"), req -> ok().body(posts.findAll()));
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
