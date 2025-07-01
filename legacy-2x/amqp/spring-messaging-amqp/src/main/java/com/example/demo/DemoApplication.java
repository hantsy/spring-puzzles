package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.LocalDateTime;

import static org.springframework.web.servlet.function.RequestPredicates.POST;
import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.ServerResponse.accepted;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@SpringBootApplication
@Slf4j
public class DemoApplication {
    public static final String DIRECT_EXCHANGE_PINGPONG = "pingpong";
    public static final String QUEUE_PINGPONG = "pingpong";
    public static final String ROUTING_KEY_PINGPONG = "r.pingpong";

    @Bean
    Queue queuePingpong() {
        return new Queue(QUEUE_PINGPONG, false);
    }

    @Bean
    DirectExchange exchangePingpong() {
        return new DirectExchange(DIRECT_EXCHANGE_PINGPONG);
    }

    @Bean
    Binding bindingPingpong(Queue queuePingpong, DirectExchange exchangePingpong) {
        return BindingBuilder.bind(queuePingpong).to(exchangePingpong).with(ROUTING_KEY_PINGPONG);
    }

    //
    public static final String ROUTING_PATTERN = "greeting.#";
    public static final String ROUTING_KEY_WELCOME = "greeting.welcome";
    public static final String ROUTING_KEY_HELLO = "greeting.hello";
    public static final String TOPIC_EXCHANGE_GREETING = "greeting";
    public static final String QUEUE_HELLO = "greeting-hello";
    public static final String QUEUE_WELCOME = "greeting-welcome";
    public static final String QUEUE_GREETING = "all-greetings";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    Queue queueGreeting() {
        return new Queue(QUEUE_GREETING, false);
    }

    @Bean
    Queue queueWelcome() {
        return new Queue(QUEUE_WELCOME, false);
    }

    @Bean
    Queue queueHello() {
        return new Queue(QUEUE_HELLO, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE_GREETING);
    }

    @Bean
    Binding bindingGreetingWelcome(Queue queueWelcome, TopicExchange exchange) {
        return BindingBuilder.bind(queueWelcome).to(exchange).with(ROUTING_KEY_WELCOME);
    }

    @Bean
    Binding bindingGreetingHello(Queue queueHello, TopicExchange exchange) {
        return BindingBuilder.bind(queueHello).to(exchange).with(ROUTING_KEY_HELLO);
    }

    @Bean
    Binding bindingAllGreetings(Queue queueGreeting, TopicExchange exchange) {
        return BindingBuilder.bind(queueGreeting).to(exchange).with(ROUTING_PATTERN);
    }

    public static final String QUEUE_LOGGER = "logger";
    public static final String EXCHANGE_LOGGER = "logger";

    @Bean
    Queue queueLogger() {
        return new Queue(QUEUE_LOGGER, false);
    }

    @Bean
    FanoutExchange exchangeLogger() {
        return new FanoutExchange(EXCHANGE_LOGGER);
    }

    @Bean
    Binding bindingLogger(Queue queueLogger, FanoutExchange exchangeLogger) {
        return BindingBuilder.bind(queueLogger).to(exchangeLogger);
    }

    //MessageConverter from spring-messaging module does not work with AmqpTemplate/RabbitTemplate.
    //Make sure it is org.springframework.amqp.support.converter.MessageConverter
    @Bean
    MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // Use the spring-messaging general MappingJackson2MessageConverter in RabbitMessagingTemplate
    // to replace Spring AMQP specific Jackson2JsonMessageConverter
    @Bean
    MappingJackson2MessageConverter mappingJackson2MessageConverter() {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        return messageConverter;
    }

    // wraps MappingJackson2MessageConverter and RabbitTemplate
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
            MessageConverter messageConverter,
            MappingJackson2MessageConverter mappingJackson2MessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);

        // but the replyTo channel can not use the Spring messaging MappingJackson2MessageConverter.
        // You have to customize the existing SimpleRabbitListenerContainerFactory bean and
        // set it to use Spring AMQP(Rabbit) specific Jackson2JsonMessageConverter manually.
        // The code looks a little ugly.
        //factory.setMessageConverter(messageConverter);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory(MappingJackson2MessageConverter mappingJackson2MessageConverter) {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(mappingJackson2MessageConverter);
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
        return route()
                .GET("/ping",
                        req -> {
                            String result = rabbitTemplate.convertSendAndReceive(
                                    DIRECT_EXCHANGE_PINGPONG,
                                    ROUTING_KEY_PINGPONG,
                                    "ping",
                                    String.class
                            );
                            log.info("response from '/ping': {}", result);
                            return ok().body(result);
                        }
                )
                .POST("/welcome",
                        req -> {
                            rabbitTemplate.convertAndSend(
                                    TOPIC_EXCHANGE_GREETING,
                                    ROUTING_KEY_WELCOME,
                                    req.body(GreetingRequest.class)
                            );
                            return accepted().build();
                        }

                )
                .POST("/hello",
                        req -> {
                            rabbitTemplate.convertAndSend(
                                    TOPIC_EXCHANGE_GREETING,
                                    ROUTING_KEY_HELLO,
                                    req.body(GreetingRequest.class)
                            );
                            return accepted().build();
                        }
                )
                .build();
    }

}


@Component
@Slf4j
class PingpongHandler {
    @RabbitListener(id = "pingpong", queues = DemoApplication.QUEUE_PINGPONG)
    public String ping(String request) {
        log.info("Received request: in : {}", request, this.getClass().getName());
        return "pong";
    }
}

@Component
@Slf4j
class GreetingHandler {

    @RabbitListener(id = "greeting", queues = DemoApplication.QUEUE_GREETING)
    public void handle(GreetingRequest request) {
        log.info("Received greeting request: {} in {}", request, this.getClass().getName());
    }

}

@Component
@Slf4j
class WelcomeHandler {
    @RabbitListener(id = "welcome", queues = DemoApplication.QUEUE_WELCOME)
    public void handle(GreetingRequest request) {
        log.info("Received greeting request: {} in {}", request, this.getClass().getName());

    }
}

@Component
@Slf4j
class HelloHandler {
    @RabbitListener(id = "hello", queues = DemoApplication.QUEUE_HELLO)
    @SendTo("logger")
    public GreetingResult handle(GreetingRequest request) {
        log.info("Received greeting request: {} in {}", request, this.getClass().getName());
        return GreetingResult.builder()
                .message("Hello, " + request.getName())
                .createdAt(LocalDateTime.now())
                .build();
    }
}

@Component
@Slf4j
class LoggerHandler {
    @RabbitListener(id = "logger", queues = DemoApplication.QUEUE_LOGGER)
    public void handle(GreetingResult request) {
        log.info("Received request: {} in {}", request, this.getClass().getName());
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GreetingRequest {
    String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class GreetingResult {
    String message;
    LocalDateTime createdAt;
}