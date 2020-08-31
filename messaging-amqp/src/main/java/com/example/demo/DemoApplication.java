package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.POST;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
public class DemoApplication {
    public static final String ROUTING_PATTERN = "barry.q.#";
    public static final String ROUTING_KEY = "barry.q.signups";
    public static final String TOPIC_EXCHANGE_NAME = "barry";
    public static final String QUEUE_NAME = "signups";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    Queue queue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_PATTERN);
    }

    //MessageConverter from spring-messaging module does not work with AmqpTemplate/RabbitTemplate.
    //Make sure it is org.springframework.amqp.support.converter.MessageConverter
    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RouterFunction<ServerResponse> router(RabbitTemplate rabbitTemplate) {
        return route(
                POST("/"),
                req -> ok()
                        .body(
                                rabbitTemplate.convertSendAndReceiveAsType(
                                        TOPIC_EXCHANGE_NAME,
                                        ROUTING_KEY,
                                        req.body(SignupRequest.class),
                                        ParameterizedTypeReference.forType(SignupResult.class)
                                )
                        )
        );
    }


    // The following is a possible solution to use spring-messaging generic MessageConverter
    // but the replyTo channel can not use this converter.
    // You have to customize a SimpleRabbitListenerContainerFactory bean and
    // set it to use Spring AMQP(Rabbit) specific Jackson2JsonMessageConverter manually.
    // The code looks a little ugly.
    /*@Bean
    MappingJackson2MessageConverter jacksonMessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    @Primary
    RabbitMessagingTemplate rabbitMessagingTemplate(MappingJackson2MessageConverter messageConverter, RabbitTemplate rabbitTemplate) {
        var messagingTemplate = new RabbitMessagingTemplate();
        messagingTemplate.setMessageConverter(messageConverter);
        messagingTemplate.setRabbitTemplate(rabbitTemplate);
        return messagingTemplate;
    }

    @Bean
    @Primary
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MappingJackson2MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        //can not use spring-messaging MessageConverter
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory(MappingJackson2MessageConverter messageConverter) {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    public RabbitListenerConfigurer rabbitListenerConfigurer(DefaultMessageHandlerMethodFactory factory) {
        return registrar -> {
            registrar.setMessageHandlerMethodFactory(factory);
        };
    }


    @Bean
    RouterFunction<ServerResponse> router(RabbitMessagingTemplate rabbitTemplate) {
        return route(
                POST("/"),
                req -> ok()
                        .body(
                                rabbitTemplate.convertSendAndReceive(
                                        TOPIC_EXCHANGE_NAME,
                                        ROUTING_KEY,
                                        req.body(SignupRequest.class),
                                        SignupResult.class
                                )
                        )


        );
    }*/

}

@Component
@RequiredArgsConstructor
@Slf4j
class SignupHandler {

    @RabbitListener(id="signup",  queues = DemoApplication.QUEUE_NAME)
    public SignupResult handle(SignupRequest request) {
        var fullName = request.getFullName();
        var phone = request.getPhone();
        var names = fullName.split("\\s");
        var firstName = names[0];
        var lastName = names[1] == null ? "" : names[1];
        var formattedPhone = "+33" + phone.replaceAll("\\s", "");

        log.info("User {} {} with phone {} has just signed up!", firstName, lastName, formattedPhone);

        return SignupResult.builder()
                .firstName(firstName)
                .lastName(lastName)
                .phone(formattedPhone)
                .build();
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SignupRequest {
    String fullName;
    String phone;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SignupResult {
    String firstName;
    String lastName;
    String phone;
}