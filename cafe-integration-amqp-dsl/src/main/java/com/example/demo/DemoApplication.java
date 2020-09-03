package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.*;
import org.springframework.integration.samples.cafe.*;
import org.springframework.integration.samples.cafe.xml.Barista;
import org.springframework.integration.samples.cafe.xml.Waiter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(DemoApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);

        Cafe cafe = ctx.getBean(Cafe.class);
        for (int i = 1; i <= 100; i++) {
            Order order = new Order(i);
            order.addItem(DrinkType.LATTE, new Random().nextInt(5)+1, false);
            order.addItem(DrinkType.MOCHA, new Random().nextInt(5)+1, true);
            cafe.placeOrder(order);
        }

        System.out.println("Hit 'Enter' to terminate");
        System.in.read();
        ctx.close();
    }

    public static final String ROUTING_PATTERN_ORDERS = "order.*";
    public static final String ROUTING_KEY = "order.new";
    public static final String TOPIC_EXCHANGE_CAFE_ORDERS = "cafe-orders";
    public static final String QUEUE_ALL_ORDERS = "all-orders";
    public static final String QUEUE_NEW_ORDERS = "new-orders";

    public static final String ROUTING_PATTERN_CAFE_DRINKS = "drink.*";
    public static final String TOPIC_EXCHANGE_CAFE_DRINKS = "cafe-drinks";
    public static final String QUEUE_ALL_DRINKS = "all-drinks";

    public static final String ROUTING_KEY_COLD_DRINKS = "drink.cold";
    public static final String QUEUE_COLD_DRINKS = "cold-drinks";
    public static final String QUEUE_ALL_COLD_DRINKS = "all-cold-drinks";

    public static final String ROUTING_KEY_HOT_DRINKS = "drink.hot";
    public static final String QUEUE_HOT_DRINKS = "hot-drinks";
    public static final String QUEUE_ALL_HOT_DRINKS = "all-hot-drinks";

    public static final String FANOUT_EXCHANGE_CAFE_DELIVERIES = "cafe-deliveries";
    public static final String QUEUE_ALL_DELIVERIES = "all-deliveries";

    //rabbit beans for orders.
    @Bean
    Queue allOrdersQueue() {
        return new Queue(QUEUE_ALL_ORDERS, false);
    }

    @Bean
    Queue newOrdersQueue() {
        return new Queue(QUEUE_NEW_ORDERS, false);
    }

    @Bean
    TopicExchange ordersExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_CAFE_ORDERS);
    }

    @Bean
    Binding allOrdersBinding(Queue allOrdersQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(allOrdersQueue).to(ordersExchange).with(ROUTING_PATTERN_ORDERS);
    }

    @Bean
    Binding newOrdersBinding(Queue newOrdersQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(newOrdersQueue).to(ordersExchange).with(ROUTING_PATTERN_ORDERS);
    }

    // rabbit beans for drinks.
    @Bean
    Queue allDrinksQueue() {
        return new Queue(QUEUE_ALL_DRINKS, false);
    }

    @Bean
    Queue allColdDrinksQueue() {
        return new Queue(QUEUE_ALL_COLD_DRINKS, false);
    }

    @Bean
    Queue allHotDrinksQueue() {
        return new Queue(QUEUE_ALL_HOT_DRINKS, false);
    }

    @Bean
    Queue coldDrinksQueue() {
        return new Queue(QUEUE_COLD_DRINKS, false);
    }

    @Bean
    Queue hotDrinksQueue() {
        return new Queue(QUEUE_HOT_DRINKS, false);
    }

    @Bean
    TopicExchange drinksExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_CAFE_DRINKS);
    }

    @Bean
    Binding allDrinksBinding(Queue allDrinksQueue, TopicExchange drinksExchange) {
        return BindingBuilder.bind(allDrinksQueue).to(drinksExchange).with(ROUTING_PATTERN_CAFE_DRINKS);
    }

    @Bean
    Binding allColdDrinksBinding(Queue allColdDrinksQueue, TopicExchange drinksExchange) {
        return BindingBuilder.bind(allColdDrinksQueue).to(drinksExchange).with(ROUTING_KEY_COLD_DRINKS);
    }

    @Bean
    Binding allHotDrinksBinding(Queue allHotDrinksQueue, TopicExchange drinksExchange) {
        return BindingBuilder.bind(allHotDrinksQueue).to(drinksExchange).with(ROUTING_KEY_HOT_DRINKS);
    }

    @Bean
    Binding coldDrinksBinding(Queue coldDrinksQueue, TopicExchange drinksExchange) {
        return BindingBuilder.bind(coldDrinksQueue).to(drinksExchange).with(ROUTING_KEY_COLD_DRINKS);
    }

    @Bean
    Binding hotDrinksBinding(Queue hotDrinksQueue, TopicExchange drinksExchange) {
        return BindingBuilder.bind(hotDrinksQueue).to(drinksExchange).with(ROUTING_KEY_HOT_DRINKS);
    }

    //  rabbit beans for deliveries
    @Bean
    Queue allDeliveriesQueue() {
        return new Queue(QUEUE_ALL_DELIVERIES, false);
    }

    @Bean
    FanoutExchange deliveriesExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE_CAFE_DELIVERIES);
    }

    @Bean
    Binding allDeliveriesBinding(Queue allDeliveriesQueue, FanoutExchange deliveriesExchange) {
        return BindingBuilder.bind(allDeliveriesQueue).to(deliveriesExchange);
    }

//    @Bean
//    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
//        return new Jackson2JsonMessageConverter(objectMapper);
//    }

    @Bean
    Barista barista() {
        return new Barista();
    }

    @Bean
    Waiter waiter() {
        return new Waiter();
    }

    @MessagingGateway
    public interface Cafe {

        @Gateway(requestChannel = "ordersFlow.input")
        void placeOrder(Order order);

    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedDelay(1000).get();
    }

    @Bean
    public IntegrationFlow ordersFlow(AmqpTemplate amqpTemplate) {
        return f -> f
                .enrichHeaders(spec -> spec.<Order>headerFunction("NUMBER", message -> message.getPayload().getNumber()))
                .channel(MessageChannels.direct("newOrders"))
                .transform(Transformers.toJson())
                .channel(MessageChannels.direct("jsonNewOrders"))
                .handle(
                        Amqp.outboundAdapter(amqpTemplate)
                                .exchangeName(TOPIC_EXCHANGE_CAFE_ORDERS)
                                .routingKeyExpression("'order.'+headers.NUMBER")
                );
    }

    @Bean
    public IntegrationFlow prepareDrinksFlow(ConnectionFactory connectionFactory) {
        return IntegrationFlows
                .from(
                        Amqp.inboundAdapter(connectionFactory, QUEUE_NEW_ORDERS)
                )
                .transform(Transformers.fromJson(Order.class))
                .channel(MessageChannels.direct("preOrders"))
                .split(Order.class, Order::getItems)
                .channel(c -> c.executor("preDrinks", Executors.newCachedThreadPool()))
                .<OrderItem, Boolean>route(OrderItem::isIced, mapping -> mapping
                        .channelMapping(true, coldDrinksChannel())
                        .channelMapping(false, hotDrinksChannel())
                )
                .get();
    }

    @Bean
    MessageChannel coldDrinksChannel() {
        return MessageChannels.queue("coldDrinks", 10).get();
    }

    @Bean
    MessageChannel hotDrinksChannel() {
        return MessageChannels.queue("hotDrinks", 10).get();
    }


    @Bean
    public IntegrationFlow coldDrinksFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows
                .from(coldDrinksChannel())
                .transform(Transformers.toJson())
                .handle(
                        Amqp.outboundGateway(amqpTemplate)
                                .exchangeName(TOPIC_EXCHANGE_CAFE_DRINKS)
                                .routingKey(ROUTING_KEY_COLD_DRINKS)
                )
                .log("[coldDrinksFlow]")
                .channel(preparedJsonDrinksChannel())
                .get();
    }

    @Bean
    public IntegrationFlow coldDrinksBaristaFlow(ConnectionFactory connectionFactory, Barista barista) {
        return IntegrationFlows
                .from(Amqp.inboundGateway(connectionFactory, QUEUE_COLD_DRINKS)
                        .configureContainer(
                                c -> c.receiveTimeout(10000)
                        )
                        .requestChannel(coldDrinksRequestChannel())
                        .replyChannel(coldDrinksReplyChannel())
                )
                .channel(coldDrinksRequestChannel())
                .transform(Transformers.fromJson(OrderItem.class))
                .handle(OrderItem.class, (payload, headers) -> barista.prepareColdDrink(payload))
                .transform(Transformers.toJson())
                .log("[coldDrinksBaristaFlow]")
                .channel(coldDrinksReplyChannel())
                .get();
    }

    @Bean
    MessageChannel coldDrinksRequestChannel() {
        return MessageChannels.direct("coldDrinksRequest").get();
    }

    @Bean
    MessageChannel coldDrinksReplyChannel() {
        return MessageChannels.direct("coldDrinksReply").get();
    }

    @Bean
    public IntegrationFlow hotDrinksFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows
                .from(hotDrinksChannel())
                .transform(Transformers.toJson())
                .handle(
                        Amqp.outboundGateway(amqpTemplate)
                                .exchangeName(TOPIC_EXCHANGE_CAFE_DRINKS)
                                .routingKey(ROUTING_KEY_HOT_DRINKS)
                )
                .log("[hotDrinksFlow]")
                .channel(preparedJsonDrinksChannel())
                .get();
    }

@Bean
public IntegrationFlow hotDrinksBaristaFlow(ConnectionFactory connectionFactory, Barista barista) {
    return IntegrationFlows
            .from(Amqp.inboundGateway(connectionFactory, QUEUE_HOT_DRINKS)
                    .configureContainer(
                            c -> c.receiveTimeout(10000)
                    )
                    .requestChannel(hotDrinksRequestChannel())
                    .replyChannel(hotDrinksReplyChannel())
            )
            .channel(hotDrinksRequestChannel())
            .transform(Transformers.fromJson(OrderItem.class))
            .handle(OrderItem.class, (payload, headers) -> barista.prepareHotDrink(payload))
            .transform(Transformers.toJson())
            .log("[hotDrinksBaristaFlow]")
            .channel(hotDrinksReplyChannel())
            .get();
}


    @Bean
    MessageChannel hotDrinksRequestChannel() {
        return MessageChannels.direct("hotDrinksRequest").get();
    }

    @Bean
    MessageChannel hotDrinksReplyChannel() {
        return MessageChannels.direct("hotDrinksReply").get();
    }

    @Bean
    MessageChannel preparedJsonDrinksChannel() {
        return MessageChannels.queue("preparedJsonDrinks", 50).get();
    }

    @Bean
    public IntegrationFlow collectDrinksFlow(Waiter waiter) {
        return IntegrationFlows
                .from(preparedJsonDrinksChannel())
                .transform(Transformers.fromJson(Drink.class))
                .channel(MessageChannels.direct("preparedDrinks"))
                .aggregate(aggregator -> aggregator
                        .outputProcessor(g -> waiter.prepareDelivery(
                                g.getMessages()
                                        .stream()
                                        .map(message -> (Drink) message.getPayload())
                                        .collect(Collectors.toList())
                                )
                        )
                        .correlationStrategy(m -> ((Drink) m.getPayload()).getOrderNumber()))
                .channel(MessageChannels.direct("preDeliveries"))
                .enrichHeaders(spec -> spec.<Delivery>headerFunction("NUMBER", m -> m.getPayload().getOrderNumber()))
                .channel(MessageChannels.direct("deliveries"))
                .transform(Transformers.toJson())
                .channel(jsonDeliveriesChannel())
                .get();
    }

    @Bean
    MessageChannel jsonDeliveriesChannel() {
        return MessageChannels.direct("jsonDeliveries").get();
    }

    @Bean
    public IntegrationFlow deliveriesFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows
                .from(jsonDeliveriesChannel())
                .transform(Transformers.fromJson(Delivery.class))
                .log("[deliveriesFlow]")
                .handle(// listen rabbit by extra listener.
                        Amqp.outboundAdapter(amqpTemplate)
                                .exchangeName(FANOUT_EXCHANGE_CAFE_DELIVERIES)
                                .routingKeyExpression("'delivery.'+headers.NUMBER")

                )
                .get();
    }

}

@Component
@Slf4j
class DeliveryTracker {

    @RabbitListener(queues = DemoApplication.QUEUE_ALL_DELIVERIES)
    public void track(Delivery deliveryJson) {
        log.info("delivery info ::\n{}", deliveryJson);
    }
}