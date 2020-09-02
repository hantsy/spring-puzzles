package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.samples.cafe.*;
import org.springframework.integration.samples.cafe.xml.Barista;
import org.springframework.integration.samples.cafe.xml.Waiter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SpringBootApplication
@ComponentScan(basePackages = "org.springframework.integration.samples.cafe")
public class DemoApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(DemoApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);

        Cafe cafe = ctx.getBean(Cafe.class);
        for (int i = 1; i <= 1; i++) {
            Order order = new Order(i);
            //order.addItem(DrinkType.LATTE, 2, false);
            order.addItem(DrinkType.MOCHA, 3, true);
            cafe.placeOrder(order);
        }

        System.out.println("Hit 'Enter' to terminate");
        System.in.read();
        ctx.close();
    }

    public static final String ROUTING_PATTERN = "order.#";
    public static final String ROUTING_KEY = "order.new";
    public static final String TOPIC_EXCHANGE_CAFE_ORDERS = "cafe-orders";
    public static final String QUEUE_ALL_ORDERS = "all-orders";
    public static final String QUEUE_NEW_ORDERS = "new-orders";

    public static final String ROUTING_PATTERN_CAFE_DRINKS = "drink.#";
    public static final String TOPIC_EXCHANGE_CAFE_DRINKS = "cafe-drinks";
    public static final String QUEUE_ALL_DRINKS = "all-drinks";

    public static final String ROUTING_KEY_COLD_DRINKS = "drink.cold";
    public static final String QUEUE_COLD_DRINKS = "cold-drinks";
    public static final String QUEUE_ALL_COLD_DRINKS = "all-cold-drinks";

    public static final String ROUTING_KEY_HOT_DRINKS = "drink.hot";
    public static final String QUEUE_HOT_DRINKS = "hot-drinks";
    public static final String QUEUE_ALL_HOT_DRINKS = "all-hot-drinks";

    public static final String FANOUT_EXCHANGE_CAFE_DELIVERIES = "cafe-delivers";
    public static final String QUEUE_ALL_DELIVERIES = "all-delivers";

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
        return BindingBuilder.bind(allOrdersQueue).to(ordersExchange).with(ROUTING_PATTERN);
    }

    @Bean
    Binding newOrdersBinding(Queue newOrdersQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(newOrdersQueue).to(ordersExchange).with(ROUTING_PATTERN);
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

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
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
                .split(Order.class, Order::getItems)
                .channel(c -> c.executor(Executors.newCachedThreadPool()))
                .<OrderItem, Boolean>route(OrderItem::isIced, mapping -> mapping
                        .channelMapping(true, MessageChannels.queue("coldDrinks", 10).get())
                        .channelMapping(false, MessageChannels.queue("hotDrinks", 10).get())

                )
                .get();
    }


    @Bean
    public IntegrationFlow coldDrinksFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows
                .from("coldDrinks")
                .handle(
                        Amqp.outboundGateway(amqpTemplate)
                                .exchangeName(TOPIC_EXCHANGE_CAFE_DRINKS)
                                .routingKey(ROUTING_KEY_COLD_DRINKS)
                )
                .log("coldDrinksFlow")
                .channel(preparedDrinksChannel())
                .get();
    }

    @Bean
    public IntegrationFlow coldDrinksBaristaFlow(ConnectionFactory connectionFactory, Barista barista) {
        return IntegrationFlows
                .from(Amqp.inboundGateway(connectionFactory, QUEUE_COLD_DRINKS)
                        .configureContainer(
                                c -> c.receiveTimeout(10000)
                        )
                )
                .handle(OrderItem.class, (payload, headers) -> (Drink) barista.prepareColdDrink(payload))
                .get();
    }

    @Bean
    public IntegrationFlow hotDrinksFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows
                .from("hotDrinks")
                .handle(
                        Amqp.outboundGateway(amqpTemplate)
                                .exchangeName(TOPIC_EXCHANGE_CAFE_DRINKS)
                                .routingKey(ROUTING_KEY_HOT_DRINKS)
                )
                .log("hotDrinksFlow")
                .channel(preparedDrinksChannel())
                .get();
    }

    @Bean
    public IntegrationFlow hotDrinksBaristaFlow(ConnectionFactory connectionFactory, Barista barista) {
        return IntegrationFlows
                .from(Amqp.inboundGateway(connectionFactory, QUEUE_HOT_DRINKS)
                        .configureContainer(
                                c -> c.receiveTimeout(10000)
                        )
                )
                .handle(OrderItem.class, (payload, headers) -> barista.prepareHotDrink(payload))
                .get();
    }

    @Bean
    MessageChannel preparedColdDrinksChannel() {
        return MessageChannels.queue("preparedColdDrinks", 50).get();
    }

    @Bean
    MessageChannel preparedHotDrinksChannel() {
        return MessageChannels.queue("preparedHotDrinks", 50).get();
    }

    @Bean
    MessageChannel preparedDrinksChannel() {
        return MessageChannels.queue("preparedDrinks", 50).get();
    }

    @Bean
    public IntegrationFlow collectDrinksFlow(Waiter waiter) {
        return IntegrationFlows
                .from(preparedDrinksChannel())
                .aggregate(aggregator -> aggregator
                        .outputProcessor(g -> waiter.prepareDelivery(
                                g.getMessages()
                                        .stream()
                                        .map(message -> (Drink) message.getPayload())
                                        .collect(Collectors.toList())
                                )
                        )
                        .correlationStrategy(m -> ((Drink) m.getPayload()).getOrderNumber()))
                .enrichHeaders(spec -> spec.<Delivery>headerFunction("NUMBER", m -> m.getPayload().getOrderNumber()))
                .channel(deliveriesChannel())
                .get();
    }

    @Bean
    MessageChannel deliveriesChannel() {
        return MessageChannels.direct("deliveries").get();
    }

    @Bean
    public IntegrationFlow deliveriesFlow(AmqpTemplate amqpTemplate) {
        return IntegrationFlows
                .from(deliveriesChannel())
                .handle(// listen rabbit by extra listener.
                        Amqp.outboundAdapter(amqpTemplate)
                                .exchangeName(FANOUT_EXCHANGE_CAFE_DELIVERIES)
                                .routingKeyExpression("'delivery.'+headers.NUMBER")

                )
                .get();
    }

/*

    @Bean
    public IntegrationFlow orders() {
        return f -> f
                .split(Order.class, Order::getItems)
                .channel(c -> c.executor(Executors.newCachedThreadPool()))
                .<OrderItem, Boolean>route(OrderItem::isIced, mapping -> mapping
                        .subFlowMapping(true, sf -> sf
                                .channel(c -> c.queue(10))
                                .publishSubscribeChannel(c -> c
                                        .subscribe(s -> s.handle(m -> sleepUninterruptibly(1, TimeUnit.SECONDS)))
                                        .subscribe(sub -> sub
                                                .<OrderItem, String>transform(p ->
                                                        Thread.currentThread().getName() +
                                                                " prepared cold drink #" +
                                                                this.coldDrinkCounter.incrementAndGet() +
                                                                " for order #" + p.getOrderNumber() + ": " + p)
                                                .handle(m -> System.out.println(m.getPayload()))))
                                .bridge())
                        .subFlowMapping(false, sf -> sf
                                .channel(c -> c.queue(10))
                                .publishSubscribeChannel(c -> c
                                        .subscribe(s -> s.handle(m -> sleepUninterruptibly(5, TimeUnit.SECONDS)))
                                        .subscribe(sub -> sub
                                                .<OrderItem, String>transform(p ->
                                                        Thread.currentThread().getName() +
                                                                " prepared hot drink #" +
                                                                this.hotDrinkCounter.incrementAndGet() +
                                                                " for order #" + p.getOrderNumber() + ": " + p)
                                                .handle(m -> System.out.println(m.getPayload()))))
                                .bridge()))
                .<OrderItem, Drink>transform(orderItem ->
                        new Drink(orderItem.getOrderNumber(),
                                orderItem.getDrinkType(),
                                orderItem.isIced(),
                                orderItem.getShots()))
                .aggregate(aggregator -> aggregator
                        .outputProcessor(g ->
                                new Delivery(g.getMessages()
                                        .stream()
                                        .map(message -> (Drink) message.getPayload())
                                        .collect(Collectors.toList())))
                        .correlationStrategy(m -> ((Drink) m.getPayload()).getOrderNumber()))
                .handle(CharacterStreamWritingMessageHandler.stdout());
    }

    private static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
        boolean interrupted = false;
        try {
            unit.sleep(sleepFor);
        } catch (InterruptedException e) {
            interrupted = true;
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
*/

}

@Component
@Slf4j
class DeliveryTracker {

    @RabbitListener(queues = DemoApplication.QUEUE_ALL_DELIVERIES)
    public void track(Delivery delivery) {
        log.info("delivery info :: {}", delivery);
    }
}