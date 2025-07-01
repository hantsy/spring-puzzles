package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.springframework.web.servlet.function.RequestPredicates.GET;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    CommandLineRunner init(PostRepository posts, JmsTemplate jmsTemplate, MailTemplate mailTemplate) {
        return args -> {
            posts.deleteAll();
            posts.save(Post.builder().title("Configure Oracle DataSource in Apache Tomcat 9").body("test content").build());
            posts.save(Post.builder().title("Spring and Jakarta EE").body("content of Spring and Jakarta EE").build());
            posts.findAll().forEach(post -> log.info("saved post:{}", post));

            var greeting = "Hello, Spring";
            log.info("sending a JMS message: {}", greeting);
            jmsTemplate.convertAndSend("testQueue", greeting);

            var message = greeting + " from Spring MailSender"
            log.info("sending an email: {}", message);
            //mail(mailSender, message);
//            mailTemplate.builder()
//                    .to("test@example.com")
//                    .from("test@example.com")
//                    .subject("test mail")
//                    .text(message)
//                    .send();
        };
    }

//    private void mail(JavaMailSender sender, String text) throws MessagingException {
//        MimeMessage message = sender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message,
//                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
//                StandardCharsets.UTF_8.name());
////        Template template = freemarkerConfig.getTemplate(templateFileName);
////        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, props);
//        helper.setTo("test@example.com");
//        helper.setText(text, true);
//        helper.setSubject("subject");
//        helper.setFrom("test@example.com");
//        sender.send(message);
//    }

    @Bean
    RouterFunction<ServerResponse> router(PostRepository posts) {
        return route(GET("/"), req -> ok().body(posts.findAll()));
    }
}

@Component
@RequiredArgsConstructor
class MailTemplate {
    private final JavaMailSender mailSender;

    Builder builder() {
        return new Builder();
    }

    class Builder {
        private String from;
        private String[] to;
        private String subject;
        private String text;

        Builder from(String _from) {
            this.from = _from;
            return this;
        }

        Builder to(String... _to) {
            this.to = _to;
            return this;
        }

        Builder subject(String _subject) {
            this.subject = _subject;
            return this;
        }

        Builder text(String _text) {
            this.text = _text;
            return this;
        }

        void send() throws MessagingException {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
//        Template template = freemarkerConfig.getTemplate(templateFileName);
//        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, props);
            helper.setTo(this.to);
            helper.setText(text, true);
            helper.setSubject(this.subject);
            helper.setFrom(this.from);
            mailSender.send(message);
        }
    }

}

@Component
@Slf4j
class GreetingReceiver {

    @JmsListener(destination = "testQueue")
    public void receiveGreeting(String greeting) {
        log.info("received greeting message: {}", greeting);
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