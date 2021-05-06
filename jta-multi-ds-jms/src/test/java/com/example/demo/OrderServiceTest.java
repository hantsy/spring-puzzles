package com.example.demo;

import com.example.demo.orders.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import javax.transaction.*;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orders;

    @Autowired
    EventLogger logger;

    @Autowired
    JmsTemplate jmsTemplate;

    @Autowired
    private TransactionManager transactionManager;

    @BeforeEach()
    public void setup() {
        this.orders.deleteAll();
        this.logger.reset();
    }

    @Test
    public void saveOrder() throws SystemException, NotSupportedException, InvalidTransactionException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        // start tx
        this.transactionManager.begin();
        var entity = OrderEntity.builder()
                .customer(new CustomerId(5L))
                .amount(1.2D)
                .build();
        var savedEntity = this.orders.save(entity);
        this.jmsTemplate.convertAndSend("ORDERS", new OrderPlacedEvent(savedEntity.getId()));

        // suspend tx
        Transaction transaction = this.transactionManager.suspend();

        // not committed yet.
        assertThat(this.orders.count()).isEqualTo(0);
        assertThat(logger.getLastEvent()).isNull();

        // resume and commit
        this.transactionManager.resume(transaction);
        this.transactionManager.commit();

        // tx committed.
        // verify the result.
        await("Wait until the test message is received")
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(logger.getLastEvent())
                        .as("Test message should have been received after transaction was committed")
                        .isNotNull()
                );
        assertThat(this.orders.count()).isEqualTo(1);
    }

}
